package com.example.catering.domain.usecase.checkout

import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyPromoCodeUseCaseTest {
    private val useCase = ApplyPromoCodeUseCase()

    @Test
    fun `blank promo code returns error`() {
        val result = useCase.execute(
            code = "   ",
            orderAmount = 2000
        )

        assertEquals(
            ApplyPromoCodeResult.Error("Введите промокод"),
            result
        )
    }

    @Test
    fun `unknown promo code returns error`() {
        val result = useCase.execute(
            code = "UNKNOWN",
            orderAmount = 2000
        )

        assertEquals(
            ApplyPromoCodeResult.Error("Неверный промокод"),
            result
        )
    }

    @Test
    fun `inactive promo code returns error`() {
        val result = useCase.execute(
            code = "OLDPROMO",
            orderAmount = 2000
        )

        assertEquals(
            ApplyPromoCodeResult.Error("Промокод больше не активен"),
            result
        )
    }

    @Test
    fun `SALE20 below minimum amount returns error`() {
        val result = useCase.execute(
            code = "SALE20",
            orderAmount = 1499
        )

        assertEquals(
            ApplyPromoCodeResult.Error(
                "Промокод доступен от 1500 ₽"
            ),
            result
        )
    }

    @Test
    fun `SALE20 at minimum amount applies 20 percent discount`() {
        val result = useCase.execute(
            code = "SALE20",
            orderAmount = 1500
        )

        assertEquals(
            ApplyPromoCodeResult.Success(
                discount = 300,
                message = "Промокод успешно применен!"
            ),
            result
        )
    }

    @Test
    fun `promo code is trimmed and case insensitive`() {
        val result = useCase.execute(
            code = "  sale20  ",
            orderAmount = 2000
        )

        assertEquals(
            ApplyPromoCodeResult.Success(
                discount = 400,
                message = "Промокод успешно применен!"
            ),
            result
        )
    }

    @Test
    fun `WELCOME200 applies fixed discount`() {
        val result = useCase.execute(
            code = "WELCOME200",
            orderAmount = 1000
        )

        assertEquals(
            ApplyPromoCodeResult.Success(
                discount = 200,
                message = "Промокод успешно применен!"
            ),
            result
        )
    }
}
