package com.example.catering.data.local.menu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface MenuCacheInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMenuCacheInfo(menuCacheInfo: MenuCacheInfoEntity)

    @Query("SELECT * FROM menu_cache_info WHERE id = :id LIMIT 1")
    suspend fun getMenuCacheInfo(id: String): MenuCacheInfoEntity?

    @Query("SELECT * FROM menu_cache_info WHERE id = :id LIMIT 1")
    fun observeMenuCacheInfo(id: String): Flow<MenuCacheInfoEntity?>
}