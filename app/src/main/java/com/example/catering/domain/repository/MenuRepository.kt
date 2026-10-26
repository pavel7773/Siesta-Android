package com.example.catering.domain.repository

import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.MenuLoadResult
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    suspend fun getMenu(): MenuLoadResult
    suspend fun getDishById(dishId: Int): Dish?
    suspend fun getDishesByIds(dishIds: List<Int>): List<Dish>
    fun observeMenu(): Flow<List<Dish>>
    fun observeLastMenuUpdatedAt(): Flow<Long?>
}