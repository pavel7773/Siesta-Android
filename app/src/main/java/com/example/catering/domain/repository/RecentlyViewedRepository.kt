package com.example.catering.domain.repository

import kotlinx.coroutines.flow.Flow

interface RecentlyViewedRepository {
    suspend fun saveRecentlyViewedDish(dishId: Int)
    fun getRecentlyViewedDishIdsFlow(): Flow<List<Int>>
    suspend fun clearRecentlyViewedDishes()

}