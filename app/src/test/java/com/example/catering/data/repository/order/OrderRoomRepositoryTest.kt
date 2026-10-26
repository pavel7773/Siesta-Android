package com.example.catering.data.repository.order


import com.example.catering.data.local.order.OrderLocalDataSource
import com.example.catering.data.remote.dto.order.CancelOrderResponse
import com.example.catering.data.remote.dto.order.CreateOrderItemRequest
import com.example.catering.data.remote.dto.order.CreateOrderRequest
import com.example.catering.data.remote.dto.order.OrderResponse
import com.example.catering.data.remote.order.OrderRemoteDataSource
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderException
import com.example.catering.domain.model.OrderStatus
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertFailsWith


class OrderRoomRepositoryTest {

    private val orderLocalDataSource =
        // «Создай поддельный OrderLocalDataSource. Настоящую Room не запускай
        mockk<OrderLocalDataSource>()

    private val orderRemoteDataSource =
        // «Создай поддельный remote source. На настоящий сервер не ходи».
        mockk<OrderRemoteDataSource>()

    private val repository = OrderRoomRepository(
        orderLocalDataSource = orderLocalDataSource,
        orderRemoteDataSource = orderRemoteDataSource
    )

    @Test
    fun `successful order creation returns server order and saves it locally`() = runTest {

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

        val inputOrder = Order(
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

        val response = OrderResponse(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            comment = "Без лука",
            deliveryType = "Доставка",
            address = "Улица 1",
            products = emptyList(),
            totalAmount = 1000,
            createdAt = 123456789L,
            statusOrder = "ACCEPTED"
        )

        val requestSlot =
            slot<CreateOrderRequest>() // slot это контейнер, который ловит аргумент, переданный в mock-функцию. Пока он пустой.
        val idempotencyKeySlot = slot<String>()
        val savedOrderSlot = slot<Order>()

        coEvery { // что mock должен ВЕРНУТЬ
            // То есть мы заранее говорим: «когда будет вызван этот метод, верни вот такой результат».
            orderRemoteDataSource.createOrder(
                request = capture(requestSlot), //capture - кладём аргумент в slot
                idempotencyKey = capture(idempotencyKeySlot)
            )
        } returns response

        coJustRun {// mock-функция Unit просто успешно ВЫПОЛНЯЕТСЯ
            orderLocalDataSource.saveOrder(capture(savedOrderSlot))
        }


        val expectedOrder = inputOrder.copy(
            id = 42,
            totalAmount = 1000,
            createdAt = 123456789L,
            statusOrder = OrderStatus.ACCEPTED
        )
        val idempotencyKey = "order-test-key-123"
        val promoCode = "SALE20"
        val result = repository.addOrder(
            order = inputOrder,
            idempotencyKey = idempotencyKey,
            promoCode = promoCode
        )

        assertEquals(expectedOrder, result)
        assertEquals(expectedOrder, savedOrderSlot.captured)

        val expectedRequest = CreateOrderRequest(
            orderItems = listOf(
                CreateOrderItemRequest(
                    dishId = 10,
                    quantity = 2
                )
            ),
            name = "Павел",
            phone = "79991234567",
            comment = "Без лука",
            deliveryType = "Доставка",
            address = "Улица 1",
            promoCode = promoCode
        )

        assertEquals(
            expectedRequest, requestSlot.captured
        )

        assertEquals(
            idempotencyKey,
            idempotencyKeySlot.captured
        )

        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.createOrder(any(), any())
        }

        coVerify(exactly = 1) {
            orderLocalDataSource.saveOrder(expectedOrder)
        }
    }

    @Test
    fun `successful order cancellation`() = runTest {

        val orderId = 42
        val response = CancelOrderResponse(
            statusOrder = "CANCELED"
        )

        val requestSlot =
            slot<Int>() // slot это контейнер, который ловит аргумент, переданный в mock-функцию. Пока он пустой.
        val orderIdSlot = slot<Int>()
        val newStatusSlot = slot<String>()

        coEvery {
            orderRemoteDataSource.cancelOrder(
                orderId = capture(requestSlot)
            )
        } returns response

        coJustRun {// mock-функция Unit просто успешно ВЫПОЛНЯЕТСЯ
            orderLocalDataSource.updateOrderStatus(
                orderId = capture(orderIdSlot),
                newStatus = capture(newStatusSlot)
            )
        }

        repository.cancelOrder(orderId = orderId)

        assertEquals(orderId, requestSlot.captured)
        assertEquals(orderId, orderIdSlot.captured)
        assertEquals(response.statusOrder, newStatusSlot.captured)

        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.cancelOrder(any())
        }

        coVerify(exactly = 1) {
            orderLocalDataSource.updateOrderStatus(
                orderId = orderId,
                newStatus = response.statusOrder
            )
        }
    }

    @Test
    fun `cancel order network error`() = runTest {

        val orderId = 42


        val requestSlot = slot<Int>()

        coEvery {
            orderRemoteDataSource.cancelOrder(
                orderId = capture(requestSlot)
            )
        } throws IOException("Network error")

        coJustRun {// mock-функция Unit просто успешно ВЫПОЛНЯЕТСЯ
            orderLocalDataSource.updateOrderStatus(
                any(),
                any()
            )
        }

        assertFailsWith<OrderException.Network> {
            repository.cancelOrder(orderId = orderId)
        }
        assertEquals(orderId, requestSlot.captured)


        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.cancelOrder(any())
        }

        coVerify(exactly = 0) {
            orderLocalDataSource.updateOrderStatus(any(), any())
        }
    }

    @Test
    fun `cancel order unauthorized returns SessionExpired`() = runTest {

        val orderId = 42
        val requestSlot = slot<Int>()
        val response401 = Response.error<Any>(
            401,
            "".toResponseBody()
        )
        val httpException401 = HttpException(response401)

        coEvery {
            orderRemoteDataSource.cancelOrder(
                orderId = capture(requestSlot)
            )
        } throws httpException401

        assertFailsWith<OrderException.SessionExpired> {
            repository.cancelOrder(orderId = orderId)
        }

        assertEquals(orderId, requestSlot.captured)


        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.cancelOrder(any())
        }

        coVerify(exactly = 0) {
            orderLocalDataSource.updateOrderStatus(any(), any())
        }
    }

    @Test
    fun `refresh orders replaces local history`() = runTest {

        val response = listOf(
            OrderResponse(
                id = 42,
                name = "Павел",
                phone = "79991234567",
                comment = "Без лука",
                deliveryType = "Доставка",
                address = "Улица 1",
                products = emptyList(),
                totalAmount = 1000,
                createdAt = 123456789L,
                statusOrder = "ACCEPTED"
            )
        )

        val expectedOrder = Order(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            comment = "Без лука",
            deliveryType = "Доставка",
            address = "Улица 1",
            products = emptyList(),
            totalAmount = 1000,
            createdAt = 123456789L,
            statusOrder = OrderStatus.ACCEPTED
        )

        val ordersSlot = slot<List<Order>>()


        coEvery {
            orderRemoteDataSource.getOrders()
        } returns response

        coJustRun {// mock-функция Unit просто успешно ВЫПОЛНЯЕТСЯ
            orderLocalDataSource.replaceOrderHistory(orders = capture(ordersSlot))
        }

        repository.refreshOrders()

        assertEquals(listOf(expectedOrder),ordersSlot.captured)

        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.getOrders()
        }
        coVerify(exactly = 1) {
            orderLocalDataSource.replaceOrderHistory(orders = any())
        }
    }

    @Test
    fun `refresh orders network error does not replace local history`() = runTest {

        coEvery {
            orderRemoteDataSource.getOrders()
        } throws IOException("Network error")

        assertFailsWith<OrderException.Network> {
            repository.refreshOrders()
        }


        coVerify(exactly = 1) { // проверяет, что suspend-функция действительно была вызвана.
            orderRemoteDataSource.getOrders()
        }

        coVerify(exactly = 0) {
            orderLocalDataSource.replaceOrderHistory(orders = any())
        }
    }

    @Test
    fun `cancel order cancellation is rethrown`() = runTest {

        val orderId = 42

        coEvery {
            orderRemoteDataSource.cancelOrder(orderId)
        } throws CancellationException("Coroutine cancelled")

        assertFailsWith<CancellationException> {
            repository.cancelOrder(orderId)
        }

        coVerify(exactly = 1) {
            orderRemoteDataSource.cancelOrder(orderId)
        }

        coVerify(exactly = 0) {
            orderLocalDataSource.updateOrderStatus(
                any(),
                any()
            )
        }
    }

}
