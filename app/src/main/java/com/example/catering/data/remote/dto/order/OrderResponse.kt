package com.example.catering.data.remote.dto.order

data class OrderResponse(
    val id: Int,
    val name: String,
    val phone: String,
    val comment: String,
    val deliveryType: String,
    val address: String,
    val products: List<OrderItemResponse>,
    val totalAmount: Long,
    val createdAt: Long,
    val statusOrder: String
)