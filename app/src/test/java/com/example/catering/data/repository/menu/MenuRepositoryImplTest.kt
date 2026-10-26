package com.example.catering.data.repository.menu

import com.example.catering.data.local.menu.MenuLocalDataSource
import com.example.catering.data.remote.menu.MenuRemoteDataSource
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.MenuDataSourceType
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CancellationException
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class MenuRepositoryImplTest {

    private lateinit var menuRemoteDataSource: MenuRemoteDataSource
    private lateinit var menuLocalDataSource: MenuLocalDataSource

    private lateinit var repository: MenuRepositoryImpl

    @Before
    fun setup() {

        menuRemoteDataSource = mockk()
        menuLocalDataSource = mockk()

        repository = MenuRepositoryImpl(
            menuRemoteDataSource = menuRemoteDataSource,
            menuLocalDataSource = menuLocalDataSource
        )
    }

    @Test
    fun `partial cache fetches remote menu and returns all requested dishes`() =
        runTest {

            val requestedIds = listOf(1, 2, 3)

            val cachedDish = createDish(
                id = 1,
                name = "Паста"
            )

            val remoteDishes = listOf(
                createDish(
                    id = 1,
                    name = "Паста"
                ),
                createDish(
                    id = 2,
                    name = "Пицца"
                ),
                createDish(
                    id = 3,
                    name = "Салат"
                )
            )

            // В локальном кэше есть только одно блюдо
            // из трёх запрошенных.
            coEvery {
                menuLocalDataSource.getMenuDishesByIds(requestedIds)
            } returns listOf(cachedDish)

            // Remote содержит полный набор.
            coEvery {
                menuRemoteDataSource.getMenu()
            } returns remoteDishes

            // getMenu() при успешной загрузке сохраняет новое меню в Room.
            coJustRun {
                menuLocalDataSource.replaceMenuDishes(
                    dishes = any(),
                    updatedAtMillis = any()
                )
            }

            val result = repository.getDishesByIds(requestedIds)

            // Repository должен вернуть ВСЕ запрошенные блюда,
            // а не только то, что случайно нашлось в кэше.
            assertEquals(
                listOf(1, 2, 3),
                result.map { it.id }
            )

            // Частичного кэша недостаточно,
            // поэтому Repository обязан обратиться к серверу.
            coVerify(exactly = 1) {
                menuRemoteDataSource.getMenu()
            }
        }

    private fun createDish(
        id: Int,
        name: String
    ): Dish {
        return Dish(
            id = id,
            name = name,
            price = 500,
            info = "Описание",
            imageUrl = "",
            weight = 300,
            calories = 500,
            proteins = 20.0,
            fats = 15.0,
            carbs = 60.0,
            category = "Горячее"
        )
    }

    @Test
    fun `full cache returns requested dishes without remote call`() = runTest {

        val requestedIds = listOf(3, 1, 2)

        val cachedDishes = listOf(
            createDish(
                id = 1,
                name = "Паста"
            ),
            createDish(
                id = 2,
                name = "Пицца"
            ),
            createDish(
                id = 3,
                name = "Салат"
            )
        )

        coEvery {
            menuLocalDataSource.getMenuDishesByIds(requestedIds)
        } returns cachedDishes

        val result = repository.getDishesByIds(requestedIds)

        // Проверяем не только состав,
        // но и сохранение порядка requestedIds.
        assertEquals(
            listOf(3, 1, 2),
            result.map { it.id }
        )

        coVerify(exactly = 1) {
            menuLocalDataSource.getMenuDishesByIds(requestedIds)
        }

        // Полный cache уже содержит все блюда,
        // поэтому network-запрос делать не нужно.
        coVerify(exactly = 0) {
            menuRemoteDataSource.getMenu()
        }
    }

    @Test
    fun `get menu cancellation is rethrown`() = runTest {

        val cachedDish = createDish(
            id = 1,
            name = "Паста"
        )

        coEvery {
            menuRemoteDataSource.getMenu()
        } throws CancellationException("Coroutine cancelled")

        // Специально даём cache.
        // Старый код после CancellationException попытается
        // использовать его как fallback — а этого делать нельзя.
        coEvery {
            menuLocalDataSource.getMenuDishes()
        } returns listOf(cachedDish)

        assertFailsWith<CancellationException> {
            repository.getMenu()
        }
        coVerify(exactly = 0) {
            menuLocalDataSource.getMenuDishes()
        }
    }

    @Test
    fun `successful remote menu is saved to cache and returned as remote`() =
        runTest {

            val remoteDishes = listOf(
                createDish(
                    id = 1,
                    name = "Паста"
                ),
                createDish(
                    id = 2,
                    name = "Пицца"
                )
            )

            // slot поймает timestamp, который Repository передаст
            // в LocalDataSource при сохранении menu.
            val updatedAtSlot = slot<Long>()

            coEvery {
                menuRemoteDataSource.getMenu()
            } returns remoteDishes

            coJustRun {
                menuLocalDataSource.replaceMenuDishes(
                    dishes = remoteDishes,
                    updatedAtMillis = capture(updatedAtSlot)
                )
            }

            val result = repository.getMenu()

            assertEquals(
                remoteDishes,
                result.dishes
            )

            assertEquals(
                MenuDataSourceType.REMOTE,
                result.source
            )

            // Repository должен вернуть тот же timestamp,
            // который использовал при сохранении cache.
            assertEquals(
                updatedAtSlot.captured,
                result.lastUpdatedAtMillis
            )

            coVerify(exactly = 1) {
                menuRemoteDataSource.getMenu()
            }

            coVerify(exactly = 1) {
                menuLocalDataSource.replaceMenuDishes(
                    dishes = remoteDishes,
                    updatedAtMillis = any()
                )
            }
        }

    @Test
    fun `remote error returns cached menu`() = runTest {

        val cachedDishes = listOf(
            createDish(
                id = 1,
                name = "Паста из cache"
            ),
            createDish(
                id = 2,
                name = "Пицца из cache"
            )
        )

        val cachedUpdatedAt = 123456789L

        coEvery {
            menuRemoteDataSource.getMenu()
        } throws IOException("No internet")

        coEvery {
            menuLocalDataSource.getMenuDishes()
        } returns cachedDishes

        coEvery {
            menuLocalDataSource.getLastMenuUpdatedAt()
        } returns cachedUpdatedAt

        val result = repository.getMenu()

        assertEquals(
            cachedDishes,
            result.dishes
        )

        assertEquals(
            MenuDataSourceType.CACHE,
            result.source
        )

        assertEquals(
            cachedUpdatedAt,
            result.lastUpdatedAtMillis
        )

        coVerify(exactly = 1) {
            menuRemoteDataSource.getMenu()
        }

        coVerify(exactly = 1) {
            menuLocalDataSource.getMenuDishes()
        }

        coVerify(exactly = 1) {
            menuLocalDataSource.getLastMenuUpdatedAt()
        }

        // При ошибке сети мы читаем существующий cache,
        // но не заменяем его несуществующими remote-данными.
        coVerify(exactly = 0) {
            menuLocalDataSource.replaceMenuDishes(
                any(),
                any()
            )
        }
    }

    @Test
    fun `remote error with empty cache rethrows original error`() = runTest {

        val networkError = IOException("No internet")

        coEvery {
            menuRemoteDataSource.getMenu()
        } throws networkError

        coEvery {
            menuLocalDataSource.getMenuDishes()
        } returns emptyList()

        val thrown = assertFailsWith<IOException> {
            repository.getMenu()
        }

        // Проверяем, что Repository не создал новую ошибку,
        // а пробросил именно исходный IOException.
        assertSame(
            networkError,
            thrown
        )

        coVerify(exactly = 1) {
            menuRemoteDataSource.getMenu()
        }

        coVerify(exactly = 1) {
            menuLocalDataSource.getMenuDishes()
        }

        // Cache пустой, поэтому timestamp читать бессмысленно.
        coVerify(exactly = 0) {
            menuLocalDataSource.getLastMenuUpdatedAt()
        }

        // Ошибка сети не должна приводить
        // к перезаписи локального menu.
        coVerify(exactly = 0) {
            menuLocalDataSource.replaceMenuDishes(
                any(),
                any()
            )
        }
    }

    @Test
    fun `get dish by id returns cached dish without remote call`() = runTest {

        val dishId = 10

        val cachedDish = createDish(
            id = dishId,
            name = "Паста"
        )

        coEvery {
            menuLocalDataSource.getMenuDishById(dishId)
        } returns cachedDish

        val result = repository.getDishById(dishId)

        assertEquals(
            cachedDish,
            result
        )

        coVerify(exactly = 1) {
            menuLocalDataSource.getMenuDishById(dishId)
        }

        // Блюдо уже найдено локально —
        // network request не нужен.
        coVerify(exactly = 0) {
            menuRemoteDataSource.getMenu()
        }
    }

    @Test
    fun `get dish by id cache miss loads menu and returns requested dish`() =
        runTest {

            val dishId = 20

            val requestedDish = createDish(
                id = 20,
                name = "Пицца"
            )

            val anotherDish = createDish(
                id = 10,
                name = "Паста"
            )

            coEvery {
                menuLocalDataSource.getMenuDishById(dishId)
            } returns null

            coEvery {
                menuRemoteDataSource.getMenu()
            } returns listOf(
                anotherDish,
                requestedDish
            )

            coJustRun {
                menuLocalDataSource.replaceMenuDishes(
                    dishes = any(),
                    updatedAtMillis = any()
                )
            }

            val result = repository.getDishById(dishId)

            assertEquals(
                requestedDish,
                result
            )

            coVerify(exactly = 1) {
                menuLocalDataSource.getMenuDishById(dishId)
            }

            coVerify(exactly = 1) {
                menuRemoteDataSource.getMenu()
            }

            coVerify(exactly = 1) {
                menuLocalDataSource.replaceMenuDishes(
                    dishes = any(),
                    updatedAtMillis = any()
                )
            }
        }

    @Test
    fun `get dish by id returns null when dish does not exist`() = runTest {

        val requestedDishId = 999

        coEvery {
            menuLocalDataSource.getMenuDishById(requestedDishId)
        } returns null

        coEvery {
            menuRemoteDataSource.getMenu()
        } returns listOf(
            createDish(
                id = 1,
                name = "Паста"
            ),
            createDish(
                id = 2,
                name = "Пицца"
            )
        )

        coJustRun {
            menuLocalDataSource.replaceMenuDishes(
                dishes = any(),
                updatedAtMillis = any()
            )
        }

        val result =
            repository.getDishById(requestedDishId)

        assertNull(result)
    }
}