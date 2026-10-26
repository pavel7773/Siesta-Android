package com.example.catering.domain.usecase.order
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateOrderUseCaseTest {

    private val orderRepository = mockk<OrderRepository>()

    private val useCase = CreateOrderUseCase(
        orderRepository = orderRepository
    )

    @Test
    fun `cancellation exception is rethrown`() = runTest {

        val order = createTestOrder()

        val idempotencyKey = "test-key-123"

        coEvery {
            orderRepository.addOrder(
                order = order,
                idempotencyKey = idempotencyKey
            )
        } throws CancellationException("Coroutine cancelled")

        assertFailsWith<CancellationException> {
            useCase.execute(
                order = order,
                idempotencyKey = idempotencyKey
            )
        }
    }

    private fun createTestOrder(): Order {

        val dish = Dish(
            id = 10,
            name = "Паста",
            price = 500,
            info = "Паста с соусом",
            imageUrl = "",
            weight = 300,
            calories = 500,
            proteins = 20.0,
            fats = 15.0,
            carbs = 60.0,
            category = "Горячее"
        )

        return Order(
            id = 0,
            name = "Павел",
            phone = "79991234567",
            comment = "Без лука",
            deliveryType = "Доставка",
            address = "Улица 1",
            products = listOf(
                CartItem(
                    dish = dish,
                    quantity = 2
                )
            ),
            totalAmount = 1000
        )
    }

    @Test
    fun `repository error returns create order error`() = runTest {

        val order = createTestOrder()

        val idempotencyKey = "test-key-123"

        coEvery {
            orderRepository.addOrder(
                order = order,
                idempotencyKey = idempotencyKey
            )
        } throws IllegalStateException("Database error")

        val result = useCase.execute(
            order = order,
            idempotencyKey = idempotencyKey
        )

        assertEquals(
            CreateOrderResult.Error("Не удалось сохранить заказ"),
            result
        )
    }

    @Test
    fun `successful creation forwards idempotency key and returns saved order`() =
        runTest {

            val order = createTestOrder()

            val savedOrder = order.copy(
                id = 42,
                createdAt = 123456789L
            )

            val idempotencyKey = "order-key-123"

            coEvery {
                orderRepository.addOrder(
                    order = order,
                    idempotencyKey = idempotencyKey
                )
            } returns savedOrder

            val result = useCase.execute(
                order = order,
                idempotencyKey = idempotencyKey
            )

            assertEquals(
                CreateOrderResult.Success(savedOrder),
                result
            )

            coVerify(exactly = 1) {
                orderRepository.addOrder(
                    order = order,
                    idempotencyKey = idempotencyKey
                )
            }
        }
}