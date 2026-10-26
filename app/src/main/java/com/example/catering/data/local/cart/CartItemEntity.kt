package com.example.catering.data.local.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cart_items")
data class CartItemEntity (
    @PrimaryKey
    val dishId: Int,
    val quantity: Int
)