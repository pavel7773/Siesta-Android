package com.example.catering.data.local.menu

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_dishes")
data class DishEntity(
    @PrimaryKey
    val id: Int,

    val name: String,
    val price: Int,
    val info: String,
    val imageUrl: String,
    val weight: Int,
    val calories: Int,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val category: String
)