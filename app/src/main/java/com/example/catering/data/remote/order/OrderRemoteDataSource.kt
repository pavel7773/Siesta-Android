package com.example.catering.data.remote.order

import com.example.catering.data.remote.dto.order.CancelOrderResponse
import com.example.catering.data.remote.dto.order.CreateOrderRequest
import com.example.catering.data.remote.dto.order.OrderResponse
import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(
    private val orderApi: OrderApi
) {

    suspend fun createOrder(
        request: CreateOrderRequest,
        idempotencyKey: String
    ): OrderResponse {
        return orderApi.createOrder(
            idempotencyKey = idempotencyKey,
            request = request
        )
    }

    suspend fun getOrders(): List<OrderResponse> {
        return orderApi.getOrders()
    }

    suspend fun cancelOrder(orderId: Int): CancelOrderResponse {
        return orderApi.cancelOrder(orderId)
    }
}