package com.example.catering.data.local.order

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.catering.domain.model.OrderStatus


@Entity("order_history")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) // не проси у меня id вручную, сам поставь следующий номер.
    val id: Int = 0, // начальное значение.генерируется сам для каждого чека

    val name: String,
    val phone: String,
    val comment: String,
    val deliveryType: String,
    val address: String,
    val totalAmount: Long,
    val createdAt: Long = 0,
    val statusOrder: String = OrderStatus.ACCEPTED.name
)
