package com.example.catering.data.repository.favorite

import com.example.catering.data.local.favorite.FavoriteLocalDataSource
import com.example.catering.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteRoomRepository @Inject constructor(
    private val favoriteLocalDataSource: FavoriteLocalDataSource
) : FavoriteRepository {
    override fun getFavoriteDishIdsFlow(): Flow<List<Int>> {
        return favoriteLocalDataSource.getFavoriteDishIdsFlow()
    }

    override suspend fun addFavoriteDish(dishId: Int) {
        favoriteLocalDataSource.addFavorite(dishId)
    }

    override suspend fun removeFavoriteDish(dishId: Int) {
        favoriteLocalDataSource.removeFavorite(dishId)
    }
}