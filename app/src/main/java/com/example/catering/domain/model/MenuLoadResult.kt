package com.example.catering.domain.model

data class MenuLoadResult(
    val dishes: List<Dish>,
    val source: MenuDataSourceType,
    val lastUpdatedAtMillis: Long?
)
enum class MenuDataSourceType {
    REMOTE,
    CACHE
}