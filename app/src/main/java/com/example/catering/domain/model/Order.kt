package com.example.catering.domain.model

data class Order(
    val id: Int,
    val name: String,
    val phone: String,
    val comment: String,
    val deliveryType: String,
    val address: String,
    val products: List<CartItem>,
    val totalAmount: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val statusOrder: OrderStatus = OrderStatus.ACCEPTED
)
