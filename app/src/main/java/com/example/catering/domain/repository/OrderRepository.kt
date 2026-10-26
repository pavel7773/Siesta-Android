package com.example.catering.domain.repository

import com.example.catering.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {

    fun getAllOrdersFlow(): Flow<List<Order>>

    suspend fun addOrder(
        order: Order,
        idempotencyKey: String,
        promoCode: String? = null
    ): Order

    suspend fun cancelOrder(orderId: Int)

    suspend fun refreshOrders()

    suspend fun clearLocalOrders()
}