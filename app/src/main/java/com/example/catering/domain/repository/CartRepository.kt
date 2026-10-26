package com.example.catering.domain.repository

import com.example.catering.domain.model.CartEntry
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getAllCartFlow(): Flow<List<CartEntry>>
    suspend fun addCart(dishId: Int, quantity: Int)
    suspend fun removeCart(dishId: Int)
    suspend fun clearCart()
    suspend fun replaceCart(
        items: List<CartEntry>
    )
    suspend fun changeQuantity(
        dishId: Int,
        delta: Int
    )
}