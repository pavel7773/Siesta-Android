package com.example.catering.data.local.order

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithDetails(
    @Embedded
    val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")

    val details: List<OrderItemEntity>
)