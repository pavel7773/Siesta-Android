package com.example.catering.data.repository.order

import com.example.catering.data.local.order.OrderLocalDataSource
import com.example.catering.data.remote.dto.order.CreateOrderItemRequest
import com.example.catering.data.remote.dto.order.CreateOrderRequest
import com.example.catering.data.remote.dto.order.OrderResponse
import com.example.catering.data.remote.order.OrderRemoteDataSource
import com.example.catering.data.remote.ApiErrorMessages
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderException
import com.example.catering.domain.model.OrderStatus
import com.example.catering.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class OrderRoomRepository @Inject constructor(
    private val orderLocalDataSource: OrderLocalDataSource,
    private val orderRemoteDataSource: OrderRemoteDataSource
) : OrderRepository {
    override fun getAllOrdersFlow(): Flow<List<Order>> {
        return orderLocalDataSource.getAllOrdersFlow()
    }

    override suspend fun addOrder(
        order: Order,
        idempotencyKey: String,
        promoCode: String?
    ): Order {

        val request = CreateOrderRequest(
            orderItems = order.products.map { cartItem ->
                CreateOrderItemRequest(
                    dishId = cartItem.dish.id,
                    quantity = cartItem.quantity
                )
            },
            name = order.name,
            phone = order.phone,
            comment = order.comment,
            deliveryType = order.deliveryType,
            address = order.address,
            promoCode = promoCode
        )

        val response = try {
            orderRemoteDataSource.createOrder(
                request = request,
                idempotencyKey = idempotencyKey
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toOrderException(creatingOrder = true)
        }

        val savedOrder = order.copy(
            id = response.id,
            totalAmount = response.totalAmount,
            createdAt = response.createdAt,
            statusOrder = OrderStatus.valueOf(response.statusOrder)
        )

        orderLocalDataSource.saveOrder(savedOrder)
        return savedOrder
    }

    override suspend fun cancelOrder(orderId: Int) {
        try {

            val response =
                orderRemoteDataSource.cancelOrder(orderId)

            orderLocalDataSource.updateOrderStatus(
                orderId,
                response.statusOrder
            )

        } catch (e: CancellationException) {

            // Отмена coroutine — не обычная ошибка.
            // Пробрасываем её дальше без преобразования.
            throw e

        } catch (e: Exception) {

            throw e.toOrderException()
        }
    }

    override suspend fun refreshOrders() {
        try {
            val responses = orderRemoteDataSource.getOrders()
            val orders = responses.map { response ->
                response.toOrder()
            }
            orderLocalDataSource.replaceOrderHistory(orders)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toOrderException()
        }
    }


    private fun OrderResponse.toOrder(): Order {
        return Order(
            id = id,
            name = name,
            phone = phone,
            comment = comment,
            deliveryType = deliveryType,
            address = address,

            products = products.map { item ->
                CartItem(
                    dish = Dish(
                        id = item.dishId,
                        name = item.name,
                        price = item.price,
                        info = "",
                        imageUrl = "",
                        weight = 0,
                        calories = 0,
                        proteins = 0.0,
                        fats = 0.0,
                        carbs = 0.0,
                        category = ""
                    ),
                    quantity = item.quantity
                )
            },

            totalAmount = totalAmount,
            createdAt = createdAt,
            statusOrder = OrderStatus.valueOf(statusOrder)
        )
    }

    private fun Throwable.toOrderException(creatingOrder: Boolean = false): OrderException {
        return when (this) {

            is HttpException -> {
                when (code()) {

                    400, 422 -> OrderException.RequestRejected(
                        if (creatingOrder) ApiErrorMessages.validation(
                            this, ApiErrorMessages.order,
                            "Проверьте состав корзины, данные доставки и промокод"
                        ) else "Не удалось выполнить действие с заказом. Обновите историю заказов"
                    )

                    401 -> OrderException.SessionExpired()

                    403 -> OrderException.RequestRejected(ApiErrorMessages.FORBIDDEN)

                    408, 504 -> OrderException.RequestRejected(
                        "Сервер не ответил вовремя. Проверьте историю заказов перед повторной попыткой"
                    )

                    404 -> OrderException.NotFound()

                    409 -> {
                        OrderException.Conflict(
                            if (creatingOrder) "Обновите историю заказов и проверьте, был ли заказ создан"
                            else ApiErrorMessages.validation(
                                this, ApiErrorMessages.cancellation,
                                "Действие с заказом сейчас невозможно. Обновите историю заказов"
                            )
                        )
                    }

                    429 -> OrderException.RequestRejected(ApiErrorMessages.RATE_LIMIT)

                    in 500..599 -> OrderException.RequestRejected(
                        "Сервис временно недоступен. Проверьте историю заказов перед повторной попыткой"
                    )

                    else -> OrderException.Unknown()
                }
            }

            is IOException -> {
                OrderException.Network()
            }

            is OrderException -> {
                this
            }

            else -> {
                OrderException.Unknown()
            }
        }
    }
    override suspend fun clearLocalOrders() {
        orderLocalDataSource.clearOrderHistory()
    }
}
