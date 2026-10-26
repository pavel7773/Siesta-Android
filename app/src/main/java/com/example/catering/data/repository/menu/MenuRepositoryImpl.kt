package com.example.catering.data.repository.menu

import com.example.catering.data.local.menu.MenuLocalDataSource
import com.example.catering.data.remote.menu.MenuRemoteDataSource
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.MenuDataSourceType
import com.example.catering.domain.model.MenuLoadResult
import com.example.catering.domain.repository.MenuRepository
import kotlinx.coroutines.CancellationException


import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class MenuRepositoryImpl @Inject constructor(
    private val menuRemoteDataSource: MenuRemoteDataSource,
    private val menuLocalDataSource: MenuLocalDataSource
) : MenuRepository {

    override suspend fun getMenu(): MenuLoadResult {

        try {
            val menu = menuRemoteDataSource.getMenu()

            val updatedAtMillis = System.currentTimeMillis()

            if (menu.isNotEmpty()) {
                menuLocalDataSource.replaceMenuDishes(
                    menu,
                    updatedAtMillis = updatedAtMillis
                )
            }

            return MenuLoadResult(
                dishes = menu,
                source = MenuDataSourceType.REMOTE,
                lastUpdatedAtMillis = updatedAtMillis
            )

        } catch (e: CancellationException) {

            // CancellationException означает отмену coroutine.
            // При отмене нельзя переходить на cache и делать вид,
            // что операция успешно продолжилась.
            throw e

        } catch (e: Exception) {

            val cachedMenu =
                menuLocalDataSource.getMenuDishes()

            if (cachedMenu.isNotEmpty()) {

                val lastUpdatedAtMillis =
                    menuLocalDataSource.getLastMenuUpdatedAt()

                return MenuLoadResult(
                    dishes = cachedMenu,
                    source = MenuDataSourceType.CACHE,
                    lastUpdatedAtMillis = lastUpdatedAtMillis
                )
            }

            throw e
        }
    }

    override suspend fun getDishById(dishId: Int): Dish? {
        val cachedDish = menuLocalDataSource.getMenuDishById(dishId)
        if (cachedDish != null) {
            return cachedDish
        }
        return getMenu().dishes.find { dish ->
            dish.id == dishId
        }
    }


    override suspend fun getDishesByIds(
        dishIds: List<Int>
    ): List<Dish> {

        if (dishIds.isEmpty()) {
            return emptyList()
        }

        val cachedDishes =
            menuLocalDataSource.getMenuDishesByIds(dishIds)

        val cachedDishesById =
            cachedDishes.associateBy { dish ->
                dish.id
            }

        val hasAllRequestedDishes =
            dishIds.all { id ->
                cachedDishesById.containsKey(id)
            }

        // Используем cache только если он содержит
        // ВСЕ запрошенные блюда.
        if (hasAllRequestedDishes) {
            return dishIds.mapNotNull { id ->
                cachedDishesById[id]
            }
        }

        // Если cache неполный — получаем актуальное меню.
        val dishesById =
            getMenu()
                .dishes
                .associateBy { dish ->
                    dish.id
                }

        return dishIds.mapNotNull { id ->
            dishesById[id]
        }
    }

    override fun observeMenu(): Flow<List<Dish>> {
        return menuLocalDataSource.observeMenuDishes()
    }

    override fun observeLastMenuUpdatedAt(): Flow<Long?> {
        return menuLocalDataSource.observeLastMenuUpdatedAt()
    }
}
