package com.example.catering.data.remote.dto.order

data class CreateOrderItemRequest (
    val dishId: Int,
    val quantity: Int
)
