package com.example.catering.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteDishIdsFlow(): Flow<List<Int>>

    suspend fun addFavoriteDish(dishId: Int)

    suspend fun removeFavoriteDish(dishId: Int)
}