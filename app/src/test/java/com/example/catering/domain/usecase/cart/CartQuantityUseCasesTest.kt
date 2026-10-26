package com.example.catering.domain.usecase.cart

import com.example.catering.domain.repository.CartRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CartQuantityUseCasesTest {

    private lateinit var cartRepository: CartRepository

    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var undoAddToCartUseCase: UndoAddToCartUseCase
    private lateinit var increaseCartItemUseCase: IncreaseCartItemUseCase
    private lateinit var decreaseCartItemUseCase: DecreaseCartItemUseCase

    @Before
    fun setup() {

        cartRepository = mockk(relaxed = true)

        addToCartUseCase =
            AddToCartUseCase(cartRepository)

        undoAddToCartUseCase =
            UndoAddToCartUseCase(cartRepository)

        increaseCartItemUseCase =
            IncreaseCartItemUseCase(cartRepository)

        decreaseCartItemUseCase =
            DecreaseCartItemUseCase(cartRepository)
    }

    @Test
    fun `add to cart changes quantity by requested count`() =
        runTest {

            addToCartUseCase.execute(
                dishId = 10,
                count = 3
            )

            coVerify(exactly = 1) {
                cartRepository.changeQuantity(
                    dishId = 10,
                    delta = 3
                )
            }
        }

    @Test
    fun `add to cart ignores zero count`() =
        runTest {

            addToCartUseCase.execute(
                dishId = 10,
                count = 0
            )

            coVerify(exactly = 0) {
                cartRepository.changeQuantity(
                    any(),
                    any()
                )
            }
        }

    @Test
    fun `add to cart ignores negative count`() =
        runTest {

            addToCartUseCase.execute(
                dishId = 10,
                count = -2
            )

            coVerify(exactly = 0) {
                cartRepository.changeQuantity(
                    any(),
                    any()
                )
            }
        }

    @Test
    fun `undo add to cart decreases quantity by one`() =
        runTest {

            undoAddToCartUseCase.execute(
                dishId = 10
            )

            coVerify(exactly = 1) {
                cartRepository.changeQuantity(
                    dishId = 10,
                    delta = -1
                )
            }
        }

    @Test
    fun `increase cart item increases quantity by one`() =
        runTest {

            increaseCartItemUseCase.execute(
                dishId = 10
            )

            coVerify(exactly = 1) {
                cartRepository.changeQuantity(
                    dishId = 10,
                    delta = 1
                )
            }
        }

    @Test
    fun `decrease cart item decreases quantity by one`() =
        runTest {

            decreaseCartItemUseCase.execute(
                dishId = 10
            )

            coVerify(exactly = 1) {
                cartRepository.changeQuantity(
                    dishId = 10,
                    delta = -1
                )
            }
        }
}