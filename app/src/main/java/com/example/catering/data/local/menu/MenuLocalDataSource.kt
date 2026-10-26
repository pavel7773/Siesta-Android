package com.example.catering.data.local.menu

import androidx.room.withTransaction
import com.example.catering.data.local.database.AppDatabase
import com.example.catering.domain.model.Dish

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MenuLocalDataSource @Inject constructor(
    private val dishDao: DishDao,
    private val appDatabase: AppDatabase,
    private val menuCacheInfoDao: MenuCacheInfoDao
) {
    private companion object {
        const val MENU_CACHE_ID = "menu"
    }
    suspend fun getMenuDishes(): List<Dish> {
        return dishEntitiesToDishes(dishDao.getMenuDishes())
    }

    fun observeMenuDishes(): Flow<List<Dish>> {
        val dishes = dishDao.observeMenuDishes()
        return dishes.map { dishEntities ->
            dishEntitiesToDishes(dishEntities)
        }
    }

    suspend fun getMenuDishById(dishId: Int): Dish? {
        val dishEntity = dishDao.getDishById(dishId)

        return if (dishEntity != null) {
            dishEntityToDish(dishEntity)
        } else {
            null
        }
    }

    suspend fun getMenuDishesByIds(dishIds: List<Int>): List<Dish> {
        val dishEntities = dishDao.getDishesByIds(dishIds)
        return dishEntitiesToDishes(dishEntities)
    }
    suspend fun replaceMenuDishes(
        dishes: List<Dish>,
        updatedAtMillis: Long
    ) {
        val dishEntities = dishesToDishEntities(dishes)
        appDatabase.withTransaction {
            dishDao.clearMenuDishes()
            dishDao.saveDishes(dishEntities)
            menuCacheInfoDao.saveMenuCacheInfo(
                MenuCacheInfoEntity(
                    id = MENU_CACHE_ID,
                    lastUpdatedAtMillis = updatedAtMillis
                )
            )
        }
    }

    suspend fun getLastMenuUpdatedAt(): Long? {
        val menuInfo = menuCacheInfoDao.getMenuCacheInfo(MENU_CACHE_ID)
        return menuInfo?.lastUpdatedAtMillis
    }

    fun observeLastMenuUpdatedAt(): Flow<Long?> {
       val menuCacheInfoFlow =  menuCacheInfoDao.observeMenuCacheInfo(MENU_CACHE_ID)
        return menuCacheInfoFlow.map { it ->
            it?.lastUpdatedAtMillis
        }
    }
}
