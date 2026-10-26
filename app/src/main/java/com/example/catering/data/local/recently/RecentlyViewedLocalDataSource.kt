package com.example.catering.data.local.recently

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecentlyViewedLocalDataSource @Inject constructor(private val recentlyViewedDao: RecentlyViewedDao) {

    suspend fun saveRecentlyViewedDish(dishId: Int) {
        val viewedDish = RecentlyViewedDishEntity(dishId, System.currentTimeMillis())
        recentlyViewedDao.insertRecentlyViewed(viewedDish)
    }

    fun getRecentlyViewedDishIdsFlow(): Flow<List<Int>> {
        return recentlyViewedDao.getAllRecentlyViewedFlow().map { it ->
            it.map { recently ->
                recently.dishId
            }

        }
    }

    suspend fun clearRecentlyViewedDishes() {
        recentlyViewedDao.clearRecentlyViewed()
    }

}