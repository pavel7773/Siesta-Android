package com.example.catering.presentation.cart

import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.usecase.cart.AddToCartUseCase
import com.example.catering.domain.usecase.cart.ClearCartUseCase
import com.example.catering.domain.usecase.cart.DecreaseCartItemUseCase
import com.example.catering.domain.usecase.cart.GetCartUseCase
import com.example.catering.domain.usecase.cart.IncreaseCartItemUseCase
import com.example.catering.domain.usecase.cart.RemoveFromCartUseCase
import com.example.catering.domain.usecase.cart.RepeatOrderResult
import com.example.catering.domain.usecase.cart.RepeatOrderUseCase
import com.example.catering.domain.usecase.cart.UndoAddToCartUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher =
        StandardTestDispatcher()

    private lateinit var cartFlow:
            MutableStateFlow<List<CartEntry>>

    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var undoAddToCartUseCase: UndoAddToCartUseCase
    private lateinit var increaseCartItemUseCase: IncreaseCartItemUseCase
    private lateinit var decreaseCartItemUseCase: DecreaseCartItemUseCase
    private lateinit var repeatOrderUseCase: RepeatOrderUseCase
    private lateinit var clearCartUseCase: ClearCartUseCase
    private lateinit var removeFromCartUseCase: RemoveFromCartUseCase

    private lateinit var viewModel: CartViewModel

    @Before
    fun setup() {

        Dispatchers.setMain(testDispatcher)

        cartFlow =
            MutableStateFlow(emptyList())

        getCartUseCase = mockk()

        addToCartUseCase =
            mockk(relaxed = true)

        undoAddToCartUseCase =
            mockk(relaxed = true)

        increaseCartItemUseCase =
            mockk(relaxed = true)

        decreaseCartItemUseCase =
            mockk(relaxed = true)

        repeatOrderUseCase =
            mockk()

        clearCartUseCase =
            mockk(relaxed = true)

        removeFromCartUseCase =
            mockk(relaxed = true)

        every {
            getCartUseCase.execute()
        } returns cartFlow

        viewModel = CartViewModel(
            getCartUseCase = getCartUseCase,
            addToCartUseCase = addToCartUseCase,
            undoAddToCartUseCase = undoAddToCartUseCase,
            increaseCartItemUseCase = increaseCartItemUseCase,
            decreaseCartItemUseCase = decreaseCartItemUseCase,
            repeatOrderUseCase = repeatOrderUseCase,
            clearCartUseCase = clearCartUseCase,
            removeFromCartUseCase = removeFromCartUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cart state follows repository flow and total count sums quantities`() =
        runTest {

            // stateIn использует WhileSubscribed.
            //
            // Поэтому без подписчика upstream Flow
            // вообще не начнёт собираться.
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.cartEntriesState.collect()
            }

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.totalItemsCount.collect()
            }

            runCurrent()

            val entries =
                listOf(
                    CartEntry(
                        dishId = 1,
                        quantity = 2
                    ),
                    CartEntry(
                        dishId = 2,
                        quantity = 3
                    )
                )

            cartFlow.value = entries

            runCurrent()

            assertEquals(
                entries,
                viewModel.cartEntriesState.value
            )

            assertEquals(
                5,
                viewModel.totalItemsCount.value
            )
        }

    @Test
    fun `cart actions delegate correct dish ids and count`() =
        runTest {

            val cartItem =
                createCartItem(
                    dishId = 10,
                    quantity = 4
                )

            viewModel.addToCart(
                dishId = 10,
                count = 3
            )

            viewModel.onUndoAddToCart(
                dishId = 10
            )

            viewModel.increaseCartItem(
                cartItem
            )

            viewModel.decreaseCartItem(
                cartItem
            )

            viewModel.removeFromCart(
                cartItem
            )

            viewModel.clearCart()

            advanceUntilIdle()

            coVerify(exactly = 1) {
                addToCartUseCase.execute(
                    dishId = 10,
                    count = 3
                )
            }

            coVerify(exactly = 1) {
                undoAddToCartUseCase.execute(
                    dishId = 10
                )
            }

            coVerify(exactly = 1) {
                increaseCartItemUseCase.execute(
                    dishId = 10
                )
            }

            coVerify(exactly = 1) {
                decreaseCartItemUseCase.execute(
                    dishId = 10
                )
            }

            coVerify(exactly = 1) {
                removeFromCartUseCase.execute(
                    dishId = 10
                )
            }

            coVerify(exactly = 1) {
                clearCartUseCase.execute()
            }
        }

    @Test
    fun `two quick increase clicks are both forwarded`() =
        runTest {

            val cartItem =
                createCartItem(
                    dishId = 10,
                    quantity = 1
                )

            viewModel.increaseCartItem(cartItem)
            viewModel.increaseCartItem(cartItem)

            advanceUntilIdle()

            // Теперь оба клика должны дойти до UseCase.
            //
            // Потерю обновлений предотвращает уже не ViewModel,
            // а атомарный changeQuantity в Room.
            coVerify(exactly = 2) {
                increaseCartItemUseCase.execute(
                    dishId = 10
                )
            }
        }

    @Test
    fun `repeat order sends correct messages and navigates only for successful results`() =
        runTest {

            val events =
                startEventCollectors()

            val order =
                createOrder()

            coEvery {
                repeatOrderUseCase.execute(order)
            } returnsMany listOf(
                RepeatOrderResult.Success,
                RepeatOrderResult.PartialSuccess,
                RepeatOrderResult.NoAvailableDishes,
                RepeatOrderResult.EmptyOrder
            )

            viewModel.repeatOrder(order)
            viewModel.repeatOrder(order)
            viewModel.repeatOrder(order)
            viewModel.repeatOrder(order)

            advanceUntilIdle()

            assertEquals(
                listOf(
                    "Заказ добавлен в корзину",
                    "Некоторые блюда из заказа больше недоступны",
                    "Блюда из этого заказа больше недоступны",
                    "Заказ пустой, повторить нечего"
                ),
                events.messages
            )

            // Success и PartialSuccess ведут в корзину.
            // Остальные два результата — нет.
            assertEquals(
                2,
                events.navigations.size
            )

            coVerify(exactly = 4) {
                repeatOrderUseCase.execute(order)
            }
        }

    @Test
    fun `repeat order unexpected error sends generic error without navigation`() =
        runTest {

            val events =
                startEventCollectors()

            val order =
                createOrder()

            coEvery {
                repeatOrderUseCase.execute(order)
            } throws IllegalStateException(
                "Database error"
            )

            viewModel.repeatOrder(order)

            advanceUntilIdle()

            assertEquals(
                listOf(
                    "Не удалось повторить заказ"
                ),
                events.messages
            )

            assertTrue(
                events.navigations.isEmpty()
            )
        }

    @Test
    fun `repeat order cancellation does not turn into generic error`() =
        runTest {

            val events =
                startEventCollectors()

            val order =
                createOrder()

            coEvery {
                repeatOrderUseCase.execute(order)
            } throws CancellationException(
                "Coroutine cancelled"
            )

            viewModel.repeatOrder(order)

            advanceUntilIdle()

            // CancellationException означает отмену работы,
            // а не ошибку, которую нужно показывать пользователю.
            assertTrue(
                events.messages.isEmpty()
            )

            assertTrue(
                events.navigations.isEmpty()
            )
        }

    private fun TestScope.startEventCollectors():
            CollectedEvents {

        val events =
            CollectedEvents()

        // Channel() без buffer — rendezvous channel.
        //
        // send() ждёт получателя.
        // Поэтому в тесте сначала запускаем collectors,
        // а уже потом вызываем repeatOrder().
        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.cartMessageEvent.collect { message ->
                events.messages.add(message)
            }
        }

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.navigateToCartEvent.collect { event ->
                events.navigations.add(event)
            }
        }

        runCurrent()

        return events
    }

    private fun createCartItem(
        dishId: Int,
        quantity: Int
    ): CartItem {

        return CartItem(
            dish = createDish(dishId),
            quantity = quantity
        )
    }

    private fun createOrder(): Order {

        return Order(
            id = 1,
            name = "Павел",
            phone = "79991234567",
            comment = "",
            deliveryType = "delivery",
            address = "Улица 1",
            products = listOf(
                createCartItem(
                    dishId = 1,
                    quantity = 2
                )
            ),
            totalAmount = 1000L
        )
    }

    private fun createDish(
        id: Int
    ): Dish {

        return Dish(
            id = id,
            name = "Блюдо $id",
            price = 500,
            info = "Описание",
            imageUrl = "",
            weight = 300,
            calories = 400,
            proteins = 20.0,
            fats = 15.0,
            carbs = 40.0,
            category = "Основное"
        )
    }

    private data class CollectedEvents(
        val messages: MutableList<String> =
            mutableListOf(),
        val navigations: MutableList<Unit> =
            mutableListOf()
    )
}