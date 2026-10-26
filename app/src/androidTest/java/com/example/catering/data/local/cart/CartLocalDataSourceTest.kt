package com.example.catering.data.local.cart

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catering.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFails

@RunWith(AndroidJUnit4::class)
class CartLocalDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var dataSource: CartLocalDataSource

    @Before
    fun setup() {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database =
            Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

        dataSource =
            CartLocalDataSource(
                cartItemDao = database.cartItemDao(),
                database = database
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceCartItems_replacesOldCartCompletely() =
        runTest {

            dataSource.saveCartItem(
                dishId = 10,
                quantity = 5
            )

            dataSource.replaceCartItems(
                listOf(
                    CartItemEntity(
                        dishId = 1,
                        quantity = 2
                    ),
                    CartItemEntity(
                        dishId = 2,
                        quantity = 3
                    )
                )
            )

            val result =
                dataSource
                    .getCartItemsFlow()
                    .first()
                    .toSet()

            assertEquals(
                setOf(
                    CartItemEntity(
                        dishId = 1,
                        quantity = 2
                    ),
                    CartItemEntity(
                        dishId = 2,
                        quantity = 3
                    )
                ),
                result
            )
        }

    @Test
    fun replaceCartItems_rollsBackWhenInsertFails() =
        runTest {

            // Старая корзина, которую нельзя потерять.
            dataSource.saveCartItem(
                dishId = 10,
                quantity = 5
            )

            // Искусственно заставляем SQLite падать
            // при попытке записать dishId = 999.
            database.openHelper
                .writableDatabase
                .execSQL(
                    """
                    CREATE TRIGGER fail_cart_insert
                    BEFORE INSERT ON cart_items
                    WHEN NEW.dishId = 999
                    BEGIN
                        SELECT RAISE(ABORT, 'forced test failure');
                    END
                    """.trimIndent()
                )

            assertFails {
                dataSource.replaceCartItems(
                    listOf(
                        CartItemEntity(
                            dishId = 1,
                            quantity = 2
                        ),
                        CartItemEntity(
                            dishId = 999,
                            quantity = 1
                        )
                    )
                )
            }

            val cartAfterFailure =
                dataSource
                    .getCartItemsFlow()
                    .first()

            assertEquals(
                listOf(
                    CartItemEntity(
                        dishId = 10,
                        quantity = 5
                    )
                ),
                cartAfterFailure
            )
        }

    @Test
    fun changeQuantity_increasesExistingQuantity() =
        runTest {

            dataSource.saveCartItem(
                dishId = 1,
                quantity = 2
            )

            dataSource.changeQuantity(
                dishId = 1,
                delta = 3
            )

            val result =
                dataSource
                    .getCartItemsFlow()
                    .first()

            assertEquals(
                listOf(
                    CartItemEntity(
                        dishId = 1,
                        quantity = 5
                    )
                ),
                result
            )
        }

    @Test
    fun changeQuantity_createsItemWhenDishIsNotInCart() =
        runTest {

            dataSource.changeQuantity(
                dishId = 1,
                delta = 3
            )

            val result =
                dataSource
                    .getCartItemsFlow()
                    .first()

            assertEquals(
                listOf(
                    CartItemEntity(
                        dishId = 1,
                        quantity = 3
                    )
                ),
                result
            )
        }

    @Test
    fun changeQuantity_removesItemWhenQuantityBecomesZero() =
        runTest {

            dataSource.saveCartItem(
                dishId = 1,
                quantity = 1
            )

            dataSource.changeQuantity(
                dishId = 1,
                delta = -1
            )

            val result =
                dataSource
                    .getCartItemsFlow()
                    .first()

            assertEquals(
                emptyList<CartItemEntity>(),
                result
            )
        }

    @Test
    fun changeQuantity_doesNotLoseConcurrentIncreases() =
        runTest {

            dataSource.saveCartItem(
                dishId = 1,
                quantity = 1
            )

            coroutineScope {

                launch(Dispatchers.Default) {
                    dataSource.changeQuantity(
                        dishId = 1,
                        delta = 1
                    )
                }

                launch(Dispatchers.Default) {
                    dataSource.changeQuantity(
                        dishId = 1,
                        delta = 1
                    )
                }
            }

            val result =
                dataSource
                    .getCartItemsFlow()
                    .first()
                    .single()

            assertEquals(
                3,
                result.quantity
            )
        }
}