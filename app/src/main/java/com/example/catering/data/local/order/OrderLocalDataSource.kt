package com.example.catering.data.local.order

import androidx.room.withTransaction
import com.example.catering.data.local.database.AppDatabase
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderLocalDataSource @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val database: AppDatabase
) {

    fun getAllOrdersFlow(): Flow<List<Order>> {
        val ordersFlow = orderDao.getAllOrdersWithDetails().map { listOfEntities ->
            listOfEntities.map { orderWithDetails ->
                val statusText = orderWithDetails.order.statusOrder
                val status = runCatching {
                    OrderStatus.valueOf(statusText.trim().uppercase())
                }.getOrDefault(OrderStatus.ACCEPTED)
                val order = Order(
                    id = orderWithDetails.order.id,
                    name = orderWithDetails.order.name,
                    phone = orderWithDetails.order.phone,
                    address = orderWithDetails.order.address,
                    comment = orderWithDetails.order.comment,
                    deliveryType = orderWithDetails.order.deliveryType,
                    products = orderWithDetails.details.map { it ->
                        val cartItem = CartItem(
                            dish = Dish(
                                id = it.dishId,
                                name = it.name,
                                price = it.price,
                                info = "",
                                imageUrl = "",
                                weight = 0,
                                calories = 0,
                                proteins = 0.0,
                                fats = 0.0,
                                carbs = 0.0,
                                category = ""
                            ),
                            quantity = it.quantity
                        )
                        cartItem
                    },
                    totalAmount = orderWithDetails.order.totalAmount,
                    createdAt = orderWithDetails.order.createdAt,
                    statusOrder = status
                )
                order
            }
        }
        return ordersFlow
    }

    suspend fun saveOrder(order: Order) {
        database.withTransaction {
            val orderEntity =
                OrderEntity(
                    id = order.id,
                    name = order.name,
                    phone = order.phone,
                    comment = order.comment,
                    deliveryType = order.deliveryType,
                    address = order.address,
                    totalAmount = order.totalAmount,
                    createdAt = order.createdAt,
                    statusOrder = order.statusOrder.name
                )
            val orderId = orderDao.insertOrder(orderEntity)
            val orderItems = order.products.map {
                val orderItemEntity = OrderItemEntity(
                    id = 0,
                    orderId = orderId.toInt(),
                    dishId = it.dish.id,
                    quantity = it.quantity,
                    name = it.dish.name,
                    price = it.dish.price

                )
                orderItemEntity
            }
            orderItemDao.insertOrderItems(orderItems)
        }
    }

    suspend fun replaceOrderHistory(orders: List<Order>) {
        database.withTransaction {
            orderItemDao.clearOrderItems()
            orderDao.clearOrders()

            orders.forEach { order ->
                val orderEntity = OrderEntity(
                    id = order.id,
                    name = order.name,
                    phone = order.phone,
                    comment = order.comment,
                    deliveryType = order.deliveryType,
                    address = order.address,
                    totalAmount = order.totalAmount,
                    createdAt = order.createdAt,
                    statusOrder = order.statusOrder.name
                )
                val orderId = orderDao.insertOrder(orderEntity)
                val orderItems = order.products.map { item ->
                    OrderItemEntity(
                        id = 0,
                        orderId = orderId.toInt(),
                        dishId = item.dish.id,
                        quantity = item.quantity,
                        name = item.dish.name,
                        price = item.dish.price
                    )
                }
                orderItemDao.insertOrderItems(orderItems)
            }
        }
    }
    suspend fun updateOrderStatus(orderId: Int, newStatus: String) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    suspend fun clearOrderHistory() {

        database.withTransaction {

            // Сначала дочерние элементы заказа,
            // затем сами заказы.
            orderItemDao.clearOrderItems()
            orderDao.clearOrders()
        }
    }
}