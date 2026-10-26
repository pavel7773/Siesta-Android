package com.example.catering.domain.model

data class PromoCode (
    val code: String,
    val discountType: PromoCodeDiscountType,
    val discountValue: Int,
    val minOrderAmount: Long,
    val isActive: Boolean
)
