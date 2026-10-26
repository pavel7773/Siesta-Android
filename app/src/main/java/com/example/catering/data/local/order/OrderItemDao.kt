package com.example.catering.data.local.order

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface OrderItemDao {

    @Insert
    suspend fun insertOrderItems(orderItemEntity: List<OrderItemEntity>)
    @Query("DELETE FROM order_item_history")
    suspend  fun clearOrderItems()
}