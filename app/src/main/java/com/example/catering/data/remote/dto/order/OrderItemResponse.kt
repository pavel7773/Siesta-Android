package com.example.catering.data.remote.dto.order

data class OrderItemResponse(
    val dishId: Int,
    val name: String,
    val price: Int,
    val quantity: Int
)