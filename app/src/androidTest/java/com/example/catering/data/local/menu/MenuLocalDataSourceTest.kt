package com.example.catering.data.local.menu

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catering.data.local.database.AppDatabase
import com.example.catering.domain.model.Dish
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuLocalDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var dataSource: MenuLocalDataSource

    @Before
    fun setup() {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        // Создаём настоящую Room database только в оперативной памяти.
        // Реальную базу приложения этот тест не трогает.
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        // Здесь уже НЕ mock.
        // DataSource работает с настоящими DAO настоящей тестовой Room DB.
        dataSource = MenuLocalDataSource(
            dishDao = database.dishDao(),
            appDatabase = database,
            menuCacheInfoDao = database.menuCacheInfoDao()
        )
    }

    @After
    fun tearDown() {

        // После каждого теста уничтожаем временную database.
        // Следующий тест получит полностью чистую БД.
        database.close()
    }

    @Test
    fun replaceMenu_removesOldDishesAndStoresNewMenuWithTimestamp() = runTest {

            val oldMenu = listOf(
                createDish(
                    id = 1,
                    name = "Старая паста"
                ),
                createDish(
                    id = 2,
                    name = "Старая пицца"
                )
            )

            val newMenu = listOf(
                createDish(
                    id = 3,
                    name = "Новая паста"
                ),
                createDish(
                    id = 4,
                    name = "Новый салат"
                )
            )

            dataSource.replaceMenuDishes(
                dishes = oldMenu,
                updatedAtMillis = 100L
            )

            dataSource.replaceMenuDishes(
                dishes = newMenu,
                updatedAtMillis = 200L
            )

            val savedMenu =
                dataSource.getMenuDishes()

            assertEquals(
                listOf(3, 4),
                savedMenu
                    .map { it.id }
                    .sorted()
            )

            assertEquals(
                200L,
                dataSource.getLastMenuUpdatedAt()
            )

            // Старое меню действительно удалено,
            // а не осталось рядом с новым.
            assertNull(
                dataSource.getMenuDishById(1)
            )
        }

    @Test
    fun getMenuDishById_mapsRoomEntityBackToDomainDish() = runTest {

            val dish = createDish(
                id = 10,
                name = "Паста Карбонара"
            )

            dataSource.replaceMenuDishes(
                dishes = listOf(dish),
                updatedAtMillis = 100L
            )

            val result =
                dataSource.getMenuDishById(10)

            assertEquals(
                dish,
                result
            )
        }

    @Test
    fun getMenuDishById_returnsNullWhenDishDoesNotExist() = runTest {

            val result =
                dataSource.getMenuDishById(999)

            assertNull(result)
        }

    @Test
    fun getDishesByIds_returnsOnlyRequestedDishes() = runTest {

            val menu = listOf(
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

            dataSource.replaceMenuDishes(
                dishes = menu,
                updatedAtMillis = 100L
            )

            val result =
                dataSource.getMenuDishesByIds(
                    listOf(1, 3)
                )

            assertEquals(
                setOf(1, 3),
                result.map { it.id }.toSet()
            )
        }

    @Test
    fun observeMenuDishes_emitsMenuStoredInRoom() = runTest {

            val menu = listOf(
                createDish(
                    id = 1,
                    name = "Паста"
                ),
                createDish(
                    id = 2,
                    name = "Пицца"
                )
            )

            dataSource.replaceMenuDishes(
                dishes = menu,
                updatedAtMillis = 100L
            )

            val emittedMenu =
                dataSource
                    .observeMenuDishes()
                    .first()

            assertEquals(
                menu.map { it.id }.toSet(),
                emittedMenu.map { it.id }.toSet()
            )
        }

    @Test
    fun replaceMenu_rollbackPreservesOldMenuWhenInsertFails() = runTest {

            val oldMenu = listOf(
                createDish(
                    id = 1,
                    name = "Старое меню"
                )
            )

            dataSource.replaceMenuDishes(
                dishes = oldMenu,
                updatedAtMillis = 111L
            )

            // Создаём специальный SQLite trigger только для теста.
            //
            // Если кто-то попытается INSERT блюдо id = 999,
            // SQLite специально выбросит ошибку.
            //
            // Так мы можем проверить настоящий rollback транзакции.
            database.openHelper
                .writableDatabase
                .execSQL(
                    """
                    CREATE TRIGGER fail_menu_insert
                    BEFORE INSERT ON menu_dishes
                    WHEN NEW.id = 999
                    BEGIN
                        SELECT RAISE(ABORT, 'forced test failure');
                    END
                    """.trimIndent()
                )

            val brokenNewMenu = listOf(
                createDish(
                    id = 999,
                    name = "Это блюдо не сохранится"
                )
            )

            val result = runCatching {

                dataSource.replaceMenuDishes(
                    dishes = brokenNewMenu,
                    updatedAtMillis = 222L
                )
            }

            assertTrue(
                result.isFailure
            )

            // Самая важная проверка.
            //
            // replaceMenuDishes сначала вызывает clearMenuDishes().
            // Но INSERT затем упал.
            //
            // Благодаря withTransaction clear тоже должен откатиться,
            // поэтому старое меню обязано остаться.
            val menuAfterFailure =
                dataSource.getMenuDishes()

            assertEquals(
                listOf(1),
                menuAfterFailure.map { it.id }
            )

            assertEquals(
                "Старое меню",
                menuAfterFailure.single().name
            )

            // Timestamp тоже является частью той же transaction.
            // Новое значение 222 не должно сохраниться.
            assertEquals(
                111L,
                dataSource.getLastMenuUpdatedAt()
            )
        }

    private fun createDish(
        id: Int,
        name: String
    ): Dish {

        return Dish(
            id = id,
            name = name,
            price = 500,
            info = "Описание блюда",
            imageUrl = "",
            weight = 300,
            calories = 500,
            proteins = 20.0,
            fats = 15.0,
            carbs = 60.0,
            category = "Горячее"
        )
    }
}