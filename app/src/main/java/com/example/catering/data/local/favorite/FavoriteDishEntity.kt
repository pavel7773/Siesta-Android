package com.example.catering.data.local.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("favorite_dishes")
data class FavoriteDishEntity(
    @PrimaryKey
    val dishId: Int
)