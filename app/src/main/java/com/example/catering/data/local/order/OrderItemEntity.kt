package com.example.catering.data.local.order

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("order_item_history")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val orderId: Int,
    val dishId: Int,
    val name: String = "", // сделали для имиграции, то есть дали ему изначсальное значение
    val quantity: Int,
    val price: Int = 0
)
