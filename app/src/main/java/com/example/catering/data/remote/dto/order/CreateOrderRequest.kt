package com.example.catering.data.remote.dto.order

data class CreateOrderRequest(
    val orderItems: List<CreateOrderItemRequest>,
    val name: String,
    val phone: String,
    val comment: String,
    val deliveryType: String,
    val address: String,
    val promoCode: String? = null
)