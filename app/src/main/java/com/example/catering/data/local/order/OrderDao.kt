package com.example.catering.data.local.order

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(orderEntity: OrderEntity): Long

    @Transaction
    @Query("SELECT * FROM order_history ORDER BY createdAt DESC, id DESC")
    fun getAllOrdersWithDetails(): Flow<List<OrderWithDetails>>

    @Query("UPDATE order_history SET statusOrder = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: String)

    @Query("DELETE FROM order_history")
    suspend fun clearOrders()
}