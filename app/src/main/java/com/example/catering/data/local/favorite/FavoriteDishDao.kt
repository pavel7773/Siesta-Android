package com.example.catering.data.local.favorite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDishDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // вставь запись, а если такая уже есть — ничего не делай
    suspend fun insertFavoriteDish(favoriteDishEntity: FavoriteDishEntity) // добавить избранное

    @Query("DELETE FROM favorite_dishes WHERE dishId = :dishId")
    suspend fun deleteFavoriteDishById(dishId: Int)

    @Query("SELECT * FROM favorite_dishes")
    fun getFavoriteDishesFlow(): Flow<List<FavoriteDishEntity>>

}