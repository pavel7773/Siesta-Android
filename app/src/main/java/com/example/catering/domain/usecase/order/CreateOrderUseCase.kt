package com.example.catering.domain.usecase.order

import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderException
import com.example.catering.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface CreateOrderResult {

    data class Success(
        val order: Order
    ) : CreateOrderResult

    data class Error(
        val message: String
    ) : CreateOrderResult
}

class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {

    suspend fun execute(
        order: Order,
        idempotencyKey: String,
        promoCode: String? = null
    ): CreateOrderResult {

        val validationError =
            validateOrder(order)

        if (validationError != null) {
            return CreateOrderResult.Error(
                validationError
            )
        }

        return try {

            val savedOrder =
                orderRepository.addOrder(
                    order = order,
                    idempotencyKey = idempotencyKey,
                    promoCode = promoCode
                )

            CreateOrderResult.Success(
                savedOrder
            )

        } catch (e: CancellationException) {

            // CancellationException означает отмену coroutine.
            // Это не обычная ошибка бизнес-логики,
            // поэтому её нельзя превращать в CreateOrderResult.Error.
            throw e

        } catch (e: Exception) {

            CreateOrderResult.Error(
                (e as? OrderException)?.message
                    ?: "Не удалось сохранить заказ"
            )
        }
    }

    private fun validateOrder(
        order: Order
    ): String? {

        if (order.products.isEmpty()) {

            return "Корзина пуста"

        } else if (
            order.name.trim().isBlank()
        ) {

            return "Введите имя"

        } else if (
            order.phone.trim().isBlank() ||
            order.phone.trim().length < 10
        ) {

            return "Введите корректный телефон"

        } else if (
            order.deliveryType == "Доставка" &&
            order.address.trim().isBlank()
        ) {

            return "Укажите адрес доставки"

        } else if (
            order.totalAmount < 0
        ) {

            return "Некорректная сумма заказа"
        }

        return null
    }
}
