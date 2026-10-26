package com.example.catering.data.local.recently

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface RecentlyViewedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // если такая строка уже есть — замени старую строку новой
    suspend fun insertRecentlyViewed(recentlyViewedDishEntity: RecentlyViewedDishEntity)

    @Query("SELECT * FROM recently_viewed_dish ORDER BY viewedAt DESC")
    fun getAllRecentlyViewedFlow(): Flow<List<RecentlyViewedDishEntity>>
//    SELECT * — получить все записи.
//    FROM recently_viewed_dish — из нужной таблицы.
//    ORDER BY viewedAt DESC — последние просмотры сверху.
    @Query("DELETE FROM recently_viewed_dish")
    suspend fun clearRecentlyViewed()
}
