package com.example.catering.data.repository.recently

import com.example.catering.data.local.recently.RecentlyViewedLocalDataSource
import com.example.catering.domain.repository.RecentlyViewedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecentlyViewedRoomRepository @Inject constructor(
    private val recentlyViewedLocalDataSource: RecentlyViewedLocalDataSource
) : RecentlyViewedRepository {
    override suspend fun saveRecentlyViewedDish(dishId: Int) {
        recentlyViewedLocalDataSource.saveRecentlyViewedDish(dishId)
    }

    override fun getRecentlyViewedDishIdsFlow(): Flow<List<Int>> {
        return recentlyViewedLocalDataSource.getRecentlyViewedDishIdsFlow()
    }

    override suspend fun clearRecentlyViewedDishes() {
        recentlyViewedLocalDataSource.clearRecentlyViewedDishes()
    }
}