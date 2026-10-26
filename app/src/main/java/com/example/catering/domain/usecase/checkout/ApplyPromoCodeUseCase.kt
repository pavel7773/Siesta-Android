package com.example.catering.domain.usecase.checkout

import com.example.catering.domain.model.PromoCode
import com.example.catering.domain.model.PromoCodeDiscountType
import javax.inject.Inject

sealed interface ApplyPromoCodeResult {
    data class Success(
        val discount: Long,
        val message: String
    ) : ApplyPromoCodeResult

    data class Error(val message: String) : ApplyPromoCodeResult
}

class ApplyPromoCodeUseCase @Inject constructor() {

    fun execute(code: String, orderAmount: Long): ApplyPromoCodeResult {
        val normalizedCode = code.trim().uppercase()

        val promoCodes = listOf(
            PromoCode(
                code = "SALE20", discountType = PromoCodeDiscountType.PERCENT,
                discountValue = 20,
                minOrderAmount = 1500,
                isActive = true
            ),
            PromoCode(
                code = "WELCOME200", discountType = PromoCodeDiscountType.FIXED_AMOUNT,
                discountValue = 200,
                minOrderAmount = 1000,
                isActive = true
            ),
            PromoCode(
                code = "BIGORDER", discountType = PromoCodeDiscountType.PERCENT,
                discountValue = 15,
                minOrderAmount = 3000,
                isActive = true
            ),
            PromoCode(
                code = "OLDPROMO", discountType = PromoCodeDiscountType.PERCENT,
                discountValue = 20,
                minOrderAmount = 0,
                isActive = false
            )
        )

        if (normalizedCode.isBlank()) {
            return ApplyPromoCodeResult.Error("Введите промокод")
        }
        val promoCode = promoCodes.firstOrNull { it.code.trim().uppercase() == normalizedCode }

        if (promoCode == null) {
            return ApplyPromoCodeResult.Error("Неверный промокод")
        } else if (!promoCode.isActive) {
            return ApplyPromoCodeResult.Error("Промокод больше не активен")
        } else if (promoCode.minOrderAmount > orderAmount) {
            return ApplyPromoCodeResult.Error("Промокод доступен от ${promoCode.minOrderAmount} ₽")
        } else {
            val discountAmount = when (promoCode.discountType) {
                PromoCodeDiscountType.PERCENT -> orderAmount * promoCode.discountValue / 100
                PromoCodeDiscountType.FIXED_AMOUNT -> minOf(orderAmount,promoCode.discountValue.toLong())
            }
            return ApplyPromoCodeResult.Success(
                discount = discountAmount,
                message = "Промокод успешно применен!"
            )
        }
    }
}
