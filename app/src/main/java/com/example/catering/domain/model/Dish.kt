package com.example.catering.domain.model

data class Dish(
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