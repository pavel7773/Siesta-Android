package com.example.catering.data.local.recently

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("recently_viewed_dish")
data class RecentlyViewedDishEntity (
    @PrimaryKey
    val dishId: Int,

    val viewedAt: Long
)

