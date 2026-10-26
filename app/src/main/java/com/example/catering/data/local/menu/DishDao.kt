package com.example.catering.data.local.menu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDishes(dishes: List<DishEntity>)

    @Query("SELECT * FROM menu_dishes")
    suspend fun getMenuDishes(): List<DishEntity>

    @Query("SELECT * FROM menu_dishes")
    fun observeMenuDishes(): Flow<List<DishEntity>>

    @Query("SELECT * FROM menu_dishes WHERE id = :dishId")
    suspend fun getDishById(dishId: Int): DishEntity?

    @Query("SELECT * FROM menu_dishes WHERE id IN (:dishIds)")
    suspend fun getDishesByIds(dishIds: List<Int>): List<DishEntity>

    @Query("DELETE FROM menu_dishes")
    suspend fun clearMenuDishes()
}