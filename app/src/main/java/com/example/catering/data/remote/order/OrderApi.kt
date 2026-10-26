package com.example.catering.data.remote.order
import com.example.catering.data.remote.dto.order.CancelOrderResponse
import com.example.catering.data.remote.dto.order.CreateOrderRequest
import com.example.catering.data.remote.dto.order.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    @POST("orders")
    suspend fun createOrder(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateOrderRequest
    ): OrderResponse

    @GET("orders")
    suspend fun getOrders(): List<OrderResponse>

    @PATCH("orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") orderId: Int
    ): CancelOrderResponse

}