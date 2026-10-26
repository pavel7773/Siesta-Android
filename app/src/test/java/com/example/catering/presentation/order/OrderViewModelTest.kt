package com.example.catering.presentation.order

import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.PendingOrderOperation
import com.example.catering.domain.repository.PendingOrderOperationStore
import com.example.catering.domain.usecase.order.CancelOrderUseCase
import com.example.catering.domain.usecase.order.CreateOrderResult
import com.example.catering.domain.usecase.order.CreateOrderUseCase
import com.example.catering.domain.usecase.order.GetAllOrdersUseCase
import com.example.catering.domain.usecase.order.RefreshOrdersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // StandardTestDispatcher — тестовый диспетчер корутин.
// Он не выполняет корутины "сам по себе" мгновенно.
// Мы сами управляем моментом их выполнения в тесте.
    private lateinit var createOrderUseCase: CreateOrderUseCase
    private lateinit var cancelOrderUseCase: CancelOrderUseCase
    private lateinit var refreshOrdersUseCase: RefreshOrdersUseCase
    private lateinit var getAllOrdersUseCase: GetAllOrdersUseCase

    private lateinit var pendingOrderOperationStore:
            FakePendingOrderOperationStore

    private lateinit var viewModel: OrderViewModel

    // @Before — код выполняется перед каждым тестом.
// setMain(...) временно заменяет Android Main dispatcher
// на тестовый, потому что обычного Android Main в JVM unit test нет.
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        createOrderUseCase = mockk()
        cancelOrderUseCase = mockk()
        refreshOrdersUseCase = mockk()
        getAllOrdersUseCase = mockk()
        pendingOrderOperationStore =
            FakePendingOrderOperationStore()
        every {
            getAllOrdersUseCase.execute()
        } returns flowOf(emptyList())
// flowOf(emptyList()) создаёт Flow,
// который сразу отдаёт пустой список заказов.
// Это нужно, потому что OrderViewModel при создании сразу подписывается
// на поток заказов через stateIn().

        viewModel = OrderViewModel(
            createOrderUseCase = createOrderUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            refreshOrdersUseCase = refreshOrdersUseCase,
            pendingOrderOperationStore = pendingOrderOperationStore,
            getOrderUseCase = getAllOrdersUseCase
        )
    }

    // @After — выполняется после каждого теста.
// resetMain() возвращает Dispatchers.Main в обычное состояние,
// чтобы один тест не влиял на другие.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `history loading reflects request lifetime and prevents duplicate refresh`() = runTest {
        val response = CompletableDeferred<Unit>()
        coEvery { refreshOrdersUseCase.execute() } coAnswers { response.await() }
        viewModel.refreshOrders()
        assertTrue(viewModel.isRefreshingOrders.value)
        runCurrent()
        viewModel.refreshOrders()
        runCurrent()
        coVerify(exactly = 1) { refreshOrdersUseCase.execute() }
        response.complete(Unit)
        advanceUntilIdle()
        assertEquals(false, viewModel.isRefreshingOrders.value)
    }

    @Test
    fun `history loading stops after failure and exposes safe error`() = runTest {
        coEvery { refreshOrdersUseCase.execute() } throws IllegalStateException("HTTP 500 internal")
        viewModel.refreshOrders()
        advanceUntilIdle()
        assertEquals(false, viewModel.isRefreshingOrders.value)
        assertEquals(OrderActionStatus.Error("Не удалось обновить историю заказов. Попробуйте позже"),
            viewModel.orderActionStatus.value)
    }

    @Test
    fun `retry after create order error reuses same idempotency key`() = runTest {

        val order = createTestOrder()

        val capturedKeys = mutableListOf<String>()
// Список, в который MockK будет складывать все idempotencyKey
// из нескольких вызовов одного и того же метода.

        coEvery {
            createOrderUseCase.execute(
                order = order,
                idempotencyKey = capture(capturedKeys)
            )
        } returns CreateOrderResult.Error("Network error")
// capture(...) перехватывает аргумент,
// который реально был передан в mock-функцию,
// и добавляет его в список capturedKeys.

        viewModel.addOrder(order,
            userId = 1)

        advanceUntilIdle()
// addOrder() запускает coroutine через viewModelScope.launch.
// advanceUntilIdle() выполняет все ожидающие тестовые корутины до конца,
// чтобы потом можно было безопасно проверять результат.
        viewModel.addOrder(order,
            userId = 1)

        advanceUntilIdle()

        assertEquals(2, capturedKeys.size)

        assertTrue(capturedKeys[0].isNotBlank())

        assertEquals(
            capturedKeys[0],
            capturedKeys[1]
        )

        coVerify(exactly = 2) {
            createOrderUseCase.execute(
                order = order,
                idempotencyKey = any()
            )
        }
    }

    @Test
    fun `retry after create order error reuses same idempotency key 1`() =
        runTest(testDispatcher) {

            val order = createTestOrder()

            // В этот список MockK положит ключ из каждого вызова execute().
            // В отличие от slot, здесь мы хотим поймать НЕ одно,
            // а несколько значений.
            val capturedKeys = mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = capture(capturedKeys)
                )
            } returns CreateOrderResult.Error("Network error")

            // Первая попытка.
            viewModel.addOrder(order,
                userId = 1)

            // viewModelScope.launch работает асинхронно.
            // Выполняем все ожидающие тестовые корутины.
            advanceUntilIdle()

            assertTrue(
                viewModel.orderSavingStatus.value is OrderSavingStatus.Error
            )

            // Retry — повторяем ТОТ ЖЕ логический заказ.
            viewModel.addOrder(order,
                userId = 1)

            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertTrue(
                capturedKeys[0].isNotBlank()
            )

            // Главная проверка:
            // после ошибки повторная попытка должна использовать тот же key.
            assertEquals(
                capturedKeys[0],
                capturedKeys[1]
            )

            coVerify(exactly = 2) {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = any()
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
    fun `successful order creation clears idempotency key for next order`() =
        runTest(testDispatcher) {

            val order = createTestOrder()

            val savedOrder = order.copy(
                id = 42
            )

            val capturedKeys = mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = capture(capturedKeys)
                )
            } returns CreateOrderResult.Success(savedOrder)

            // Первый заказ.
            viewModel.addOrder(order,
                userId = 1)
            advanceUntilIdle()

            assertTrue(
                viewModel.orderSavingStatus.value is OrderSavingStatus.Success
            )

            // После Success старый idempotencyKey должен быть очищен.
            // Поэтому следующий addOrder должен создать уже новый ключ.
            viewModel.addOrder(order,
                userId = 1)
            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertTrue(capturedKeys[0].isNotBlank())
            assertTrue(capturedKeys[1].isNotBlank())

            assertNotEquals(
                capturedKeys[0],
                capturedKeys[1]
            )

            coVerify(exactly = 2) {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = any()
                )
            }
        }

    @Test
    fun `second add order while loading does not create another request`() =
        runTest(testDispatcher) {

            val order = createTestOrder()

            val savedOrder = order.copy(
                id = 42
            )

            val resultDeferred = CompletableDeferred<CreateOrderResult>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = any()
                )
            } coAnswers {
                resultDeferred.await()
            }

            // Первый запрос.
            viewModel.addOrder(order,
                userId = 1)

            // Запускаем coroutine до момента,
            // когда она остановится на resultDeferred.await().
            runCurrent()

            assertEquals(
                OrderSavingStatus.Loading,
                viewModel.orderSavingStatus.value
            )

            // Пользователь нажимает кнопку второй раз,
            // пока первый запрос ещё выполняется.
            viewModel.addOrder(order,
                userId = 1)

            runCurrent()

            // UseCase должен быть вызван только один раз.
            coVerify(exactly = 1) {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = any()
                )
            }

            // Теперь разрешаем первому запросу закончиться.
            resultDeferred.complete(
                CreateOrderResult.Success(savedOrder)
            )

            advanceUntilIdle()

            val status = viewModel.orderSavingStatus.value

            assertTrue(status is OrderSavingStatus.Success)

            assertEquals(
                savedOrder,
                (status as OrderSavingStatus.Success).order
            )
        }

    @Test
    fun `changed order after create error uses new idempotency key`() =
        runTest(testDispatcher) {

            val firstOrder = createTestOrder()

            val changedOrder = firstOrder.copy(
                address = "Улица 2"
            )

            val capturedKeys = mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = any(),
                    idempotencyKey = capture(capturedKeys)
                )
            } returns CreateOrderResult.Error("Network error")

            // Первая попытка.
            viewModel.addOrder(firstOrder,
                userId = 1)
            advanceUntilIdle()

            // После ошибки пользователь изменил заказ.
            viewModel.addOrder(changedOrder,
                userId = 1)
            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertTrue(capturedKeys[0].isNotBlank())
            assertTrue(capturedKeys[1].isNotBlank())

            // Если данные заказа изменились,
            // это уже новая логическая операция.
            // Старый Idempotency-Key использовать нельзя.
            assertNotEquals(
                capturedKeys[0],
                capturedKeys[1]
            )

            coVerify(exactly = 1) {
                createOrderUseCase.execute(
                    order = firstOrder,
                    idempotencyKey = any()
                )
            }

            coVerify(exactly = 1) {
                createOrderUseCase.execute(
                    order = changedOrder,
                    idempotencyKey = any()
                )
            }
        }

    @Test
    fun `same order after view model recreation keeps idempotency key`() =
        runTest {

            val order = createTestOrder()

            val capturedKeys =
                mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = capture(capturedKeys)
                )
            } returns CreateOrderResult.Error(
                "Network error"
            )

            // Первый экземпляр ViewModel.
            val firstViewModel = viewModel

            firstViewModel.addOrder(order,
                userId = 1)

            advanceUntilIdle()

            assertEquals(
                1,
                capturedKeys.size
            )

            // Новый экземпляр ViewModel:
            // имитируем потерю старого состояния.
            val secondViewModel = OrderViewModel(
                    createOrderUseCase = createOrderUseCase,
                    cancelOrderUseCase = cancelOrderUseCase,
                    refreshOrdersUseCase = refreshOrdersUseCase,
                    pendingOrderOperationStore = pendingOrderOperationStore,
                    getOrderUseCase = getAllOrdersUseCase
                )


            secondViewModel.addOrder(order,
                userId = 1)

            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertEquals(
                capturedKeys[0],
                capturedKeys[1]
            )
        }

    private class FakePendingOrderOperationStore :
        PendingOrderOperationStore {

        private var operation:
                PendingOrderOperation? = null

        override suspend fun load():
                PendingOrderOperation? {
            return operation
        }

        override suspend fun save(
            operation: PendingOrderOperation
        ) {
            this.operation = operation
        }

        override suspend fun clear() {
            operation = null
        }
    }
    @Test
    fun `same order for another user uses new idempotency key`() =
        runTest(testDispatcher) {

            val order = createTestOrder()

            val capturedKeys =
                mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = capture(capturedKeys)
                )
            } returns CreateOrderResult.Error(
                "Network error"
            )

            // Пользователь A.
            viewModel.addOrder(
                order = order,
                userId = 1
            )

            advanceUntilIdle()

            // Та же persistent pending operation,
            // но уже другой аккаунт.
            val secondViewModel = OrderViewModel(
                createOrderUseCase = createOrderUseCase,
                cancelOrderUseCase = cancelOrderUseCase,
                refreshOrdersUseCase = refreshOrdersUseCase,
                pendingOrderOperationStore =
                    pendingOrderOperationStore,
                getOrderUseCase = getAllOrdersUseCase
            )

            secondViewModel.addOrder(
                order = order,
                userId = 2
            )

            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertNotEquals(
                capturedKeys[0],
                capturedKeys[1]
            )
        }
    @Test
    fun `changing promo code creates new idempotency key`() =
        runTest(testDispatcher) {

            val order = createTestOrder()

            val capturedKeys =
                mutableListOf<String>()

            coEvery {
                createOrderUseCase.execute(
                    order = order,
                    idempotencyKey = capture(capturedKeys),
                    promoCode = any()
                )
            } returns CreateOrderResult.Error(
                "Network error"
            )

            // Первая попытка — с промокодом.
            viewModel.addOrder(
                order = order,
                userId = 1,
                promoCode = "sale20"
            )

            advanceUntilIdle()

            // Повторная попытка того же заказа,
            // но промокод уже убран.
            viewModel.addOrder(
                order = order,
                userId = 1,
                promoCode = null
            )

            advanceUntilIdle()

            assertEquals(
                2,
                capturedKeys.size
            )

            assertNotEquals(
                capturedKeys[0],
                capturedKeys[1]
            )
        }
}
