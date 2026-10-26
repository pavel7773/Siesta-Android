package com.example.catering.data.local.favorite

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteLocalDataSource @Inject constructor(
    private val favoriteDishDao: FavoriteDishDao
) {

    suspend fun addFavorite(dishId: Int) {
        val favorite = FavoriteDishEntity(dishId = dishId)
        favoriteDishDao.insertFavoriteDish(favorite)
    }

    suspend fun removeFavorite(dishId: Int) {
        favoriteDishDao.deleteFavoriteDishById(dishId)
    }

    fun getFavoriteDishIdsFlow(): Flow<List<Int>> {
        return favoriteDishDao.getFavoriteDishesFlow().map {
            it.map { favoriteDishEntity ->
                favoriteDishEntity.dishId
            }
        }
    }
}