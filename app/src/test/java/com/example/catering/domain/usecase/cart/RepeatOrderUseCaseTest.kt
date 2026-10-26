package com.example.catering.domain.usecase.cart

import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.repository.CartRepository
import com.example.catering.domain.usecase.dish.GetDishesByIdsUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class RepeatOrderUseCaseTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var getDishesByIdsUseCase: GetDishesByIdsUseCase

    private lateinit var useCase: RepeatOrderUseCase

    @Before
    fun setup() {

        cartRepository = mockk()
        getDishesByIdsUseCase = mockk()

        useCase = RepeatOrderUseCase(
            cartRepository = cartRepository,
            getDishesByIdsUseCase = getDishesByIdsUseCase
        )
    }

    @Test
    fun `empty order returns empty order without touching menu or cart`() =
        runTest {

            val order = createOrder(
                products = emptyList()
            )

            val result =
                useCase.execute(order)

            assertEquals(
                RepeatOrderResult.EmptyOrder,
                result
            )

            coVerify(exactly = 0) {
                getDishesByIdsUseCase.execute(any())
            }

            coVerify(exactly = 0) {
                cartRepository.replaceCart(any())
            }
        }

    @Test
    fun `all dishes available clears cart restores quantities and returns success`() =
        runTest {

            val firstItem = createCartItem(
                dishId = 1,
                quantity = 2
            )

            val secondItem = createCartItem(
                dishId = 2,
                quantity = 3
            )

            val order = createOrder(
                products = listOf(
                    firstItem,
                    secondItem
                )
            )

            coEvery {
                getDishesByIdsUseCase.execute(
                    listOf(1, 2)
                )
            } returns listOf(
                createDish(1),
                createDish(2)
            )

            coJustRun {
                cartRepository.replaceCart(any())
            }

            val result =
                useCase.execute(order)

            assertEquals(
                RepeatOrderResult.Success,
                result
            )

            coVerify(exactly = 1) {
                getDishesByIdsUseCase.execute(
                    listOf(1, 2)
                )
            }

            coVerify(exactly = 1) {
                cartRepository.replaceCart(
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
                )
            }
        }

    @Test
    fun `missing some dishes restores only available dishes and returns partial success`() =
        runTest {

            val firstItem = createCartItem(
                dishId = 1,
                quantity = 2
            )

            val missingItem = createCartItem(
                dishId = 2,
                quantity = 5
            )

            val thirdItem = createCartItem(
                dishId = 3,
                quantity = 1
            )

            val order = createOrder(
                products = listOf(
                    firstItem,
                    missingItem,
                    thirdItem
                )
            )

            // Блюдо №2 больше отсутствует
            // в актуальном меню.
            coEvery {
                getDishesByIdsUseCase.execute(
                    listOf(1, 2, 3)
                )
            } returns listOf(
                createDish(1),
                createDish(3)
            )

            coJustRun {
                cartRepository.replaceCart(any())
            }

            val result =
                useCase.execute(order)

            assertEquals(
                RepeatOrderResult.PartialSuccess,
                result
            )

            coVerify(exactly = 1) {
                cartRepository.replaceCart(
                    listOf(
                        CartEntry(
                            dishId = 1,
                            quantity = 2
                        ),
                        CartEntry(
                            dishId = 3,
                            quantity = 1
                        )
                    )
                )
            }
        }

    @Test
    fun `no available dishes keeps existing cart and returns no available dishes`() =
        runTest {

            val order = createOrder(
                products = listOf(
                    createCartItem(
                        dishId = 1,
                        quantity = 2
                    ),
                    createCartItem(
                        dishId = 2,
                        quantity = 1
                    )
                )
            )

            coEvery {
                getDishesByIdsUseCase.execute(
                    listOf(1, 2)
                )
            } returns emptyList()

            val result =
                useCase.execute(order)

            assertEquals(
                RepeatOrderResult.NoAvailableDishes,
                result
            )

            coVerify(exactly = 0) {
                cartRepository.replaceCart(any())
            }
        }

    @Test
    fun `cancellation while loading current dishes is rethrown and cart is untouched`() =
        runTest {

            val order = createOrder(
                products = listOf(
                    createCartItem(
                        dishId = 1,
                        quantity = 2
                    )
                )
            )

            coEvery {
                getDishesByIdsUseCase.execute(
                    listOf(1)
                )
            } throws CancellationException(
                "Coroutine cancelled"
            )

            assertFailsWith<CancellationException> {
                useCase.execute(order)
            }

            coVerify(exactly = 0) {
                cartRepository.replaceCart(any())
            }
        }

    @Test
    fun `menu loading error is propagated and cart is untouched`() =
        runTest {

            val order = createOrder(
                products = listOf(
                    createCartItem(
                        dishId = 1,
                        quantity = 2
                    )
                )
            )

            val expectedError =
                IllegalStateException(
                    "Menu unavailable"
                )

            coEvery {
                getDishesByIdsUseCase.execute(
                    listOf(1)
                )
            } throws expectedError

            val actualError =
                assertFailsWith<IllegalStateException> {
                    useCase.execute(order)
                }

            assertSame(
                expectedError,
                actualError
            )

            coVerify(exactly = 0) {
                cartRepository.clearCart()
            }

            coVerify(exactly = 0) {
                cartRepository.addCart(
                    any(),
                    any()
                )
            }
        }

    private fun createOrder(
        products: List<CartItem>
    ): Order {

        return Order(
            id = 1,
            name = "Павел",
            phone = "79991234567",
            comment = "",
            deliveryType = "delivery",
            address = "Улица 1",
            products = products,
            totalAmount = 1000L
        )
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
}