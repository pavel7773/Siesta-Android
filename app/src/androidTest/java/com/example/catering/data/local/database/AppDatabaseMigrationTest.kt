package com.example.catering.data.local.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private lateinit var context: Context
    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var database: SupportSQLiteDatabase

    private val databaseName = "migration-test.db"

    @Before
    fun setup() {

        context =
            ApplicationProvider.getApplicationContext()

        // Перед каждым тестом удаляем старую тестовую БД,
        // чтобы каждый migration test начинался с чистого состояния.
        context.deleteDatabase(databaseName)

        helper = FrameworkSQLiteOpenHelperFactory()
            .create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(databaseName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(1) {

                            override fun onCreate(
                                db: SupportSQLiteDatabase
                            ) {
                                // Таблицы создаём вручную внутри тестов,
                                // потому что старых Room schema 5-8 у нас нет.
                            }

                            override fun onUpgrade(
                                db: SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int
                            ) {
                            }
                        }
                    )
                    .build()
            )

        database = helper.writableDatabase
    }

    @After
    fun tearDown() {
        helper.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration5To6_addsCreatedAtAndPreservesOrder() {

        createVersion5OrderTable()

        database.execSQL(
            """
            INSERT INTO order_history (
                id,
                name,
                phone,
                comment,
                deliveryType,
                address,
                totalAmount
            )
            VALUES (
                1,
                'Павел',
                '79991234567',
                'Без лука',
                'Доставка',
                'Улица 1',
                1000
            )
            """.trimIndent()
        )

        MIGRATION_5_6.migrate(database)

        val columns =
            getColumnNames("order_history")

        assertTrue(
            "createdAt" in columns
        )

        database.query(
            """
            SELECT name, totalAmount, createdAt
            FROM order_history
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->

            assertTrue(cursor.moveToFirst())

            assertEquals(
                "Павел",
                cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                )
            )

            assertEquals(
                1000L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("totalAmount")
                )
            )

            // Миграция задаёт DEFAULT 0.
            assertEquals(
                0L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("createdAt")
                )
            )
        }
    }

    @Test
    fun migration6To7_addsAcceptedStatusAndPreservesExistingData() {

        createVersion6OrderTable()

        database.execSQL(
            """
            INSERT INTO order_history (
                id,
                name,
                phone,
                comment,
                deliveryType,
                address,
                totalAmount,
                createdAt
            )
            VALUES (
                1,
                'Павел',
                '79991234567',
                '',
                'Доставка',
                'Улица 1',
                1500,
                123456789
            )
            """.trimIndent()
        )

        MIGRATION_6_7.migrate(database)

        val columns =
            getColumnNames("order_history")

        assertTrue(
            "statusOrder" in columns
        )

        database.query(
            """
            SELECT createdAt, statusOrder
            FROM order_history
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->

            assertTrue(cursor.moveToFirst())

            // Старое поле должно сохраниться.
            assertEquals(
                123456789L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("createdAt")
                )
            )

            // Для существующих заказов migration
            // должна поставить DEFAULT 'ACCEPTED'.
            assertEquals(
                "ACCEPTED",
                cursor.getString(
                    cursor.getColumnIndexOrThrow("statusOrder")
                )
            )
        }
    }

    @Test
    fun migration7To8_createsMenuDishesTable() {

        MIGRATION_7_8.migrate(database)

        assertTrue(
            tableExists("menu_dishes")
        )

        val columns =
            getColumnNames("menu_dishes")

        assertEquals(
            setOf(
                "id",
                "name",
                "price",
                "info",
                "imageUrl",
                "weight",
                "calories",
                "proteins",
                "fats",
                "carbs",
                "category"
            ),
            columns
        )

        // Проверяем не только структуру:
        // новая таблица должна реально принимать данные.
        database.execSQL(
            """
            INSERT INTO menu_dishes (
                id,
                name,
                price,
                info,
                imageUrl,
                weight,
                calories,
                proteins,
                fats,
                carbs,
                category
            )
            VALUES (
                10,
                'Паста',
                500,
                'Описание',
                '',
                300,
                500,
                20.0,
                15.0,
                60.0,
                'Горячее'
            )
            """.trimIndent()
        )

        database.query(
            "SELECT name FROM menu_dishes WHERE id = 10"
        ).use { cursor ->

            assertTrue(cursor.moveToFirst())

            assertEquals(
                "Паста",
                cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                )
            )
        }
    }

    @Test
    fun migration8To9_createsMenuCacheInfoTable() {

        MIGRATION_8_9.migrate(database)

        assertTrue(
            tableExists("menu_cache_info")
        )

        assertEquals(
            setOf(
                "id",
                "lastUpdatedAtMillis"
            ),
            getColumnNames("menu_cache_info")
        )

        database.execSQL(
            """
            INSERT INTO menu_cache_info (
                id,
                lastUpdatedAtMillis
            )
            VALUES (
                'menu',
                123456789
            )
            """.trimIndent()
        )

        database.query(
            """
            SELECT lastUpdatedAtMillis
            FROM menu_cache_info
            WHERE id = 'menu'
            """.trimIndent()
        ).use { cursor ->

            assertTrue(cursor.moveToFirst())

            assertEquals(
                123456789L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        "lastUpdatedAtMillis"
                    )
                )
            )
        }
    }

    @Test
    fun migration5To9_preservesOrderAndCreatesNewMenuTables() {

        createVersion5OrderTable()

        database.execSQL(
            """
            INSERT INTO order_history (
                id,
                name,
                phone,
                comment,
                deliveryType,
                address,
                totalAmount
            )
            VALUES (
                42,
                'Павел',
                '79991234567',
                '',
                'Доставка',
                'Улица 1',
                2000
            )
            """.trimIndent()
        )

        // Имитируем последовательное обновление приложения:
        //
        // v5 → v6 → v7 → v8 → v9
        MIGRATION_5_6.migrate(database)
        MIGRATION_6_7.migrate(database)
        MIGRATION_7_8.migrate(database)
        MIGRATION_8_9.migrate(database)

        assertTrue(
            tableExists("menu_dishes")
        )

        assertTrue(
            tableExists("menu_cache_info")
        )

        database.query(
            """
            SELECT
                name,
                totalAmount,
                createdAt,
                statusOrder
            FROM order_history
            WHERE id = 42
            """.trimIndent()
        ).use { cursor ->

            assertTrue(cursor.moveToFirst())

            assertEquals(
                "Павел",
                cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                )
            )

            assertEquals(
                2000L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("totalAmount")
                )
            )

            assertEquals(
                0L,
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("createdAt")
                )
            )

            assertEquals(
                "ACCEPTED",
                cursor.getString(
                    cursor.getColumnIndexOrThrow("statusOrder")
                )
            )
        }
    }

    private fun createVersion5OrderTable() {

        database.execSQL(
            """
            CREATE TABLE order_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                comment TEXT NOT NULL,
                deliveryType TEXT NOT NULL,
                address TEXT NOT NULL,
                totalAmount INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createVersion6OrderTable() {

        database.execSQL(
            """
            CREATE TABLE order_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                comment TEXT NOT NULL,
                deliveryType TEXT NOT NULL,
                address TEXT NOT NULL,
                totalAmount INTEGER NOT NULL,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    private fun tableExists(
        tableName: String
    ): Boolean {

        database.query(
            """
            SELECT name
            FROM sqlite_master
            WHERE type = 'table'
            AND name = '$tableName'
            """.trimIndent()
        ).use { cursor ->

            return cursor.moveToFirst()
        }
    }

    private fun getColumnNames(
        tableName: String
    ): Set<String> {

        val columns =
            mutableSetOf<String>()

        database.query(
            "PRAGMA table_info(`$tableName`)"
        ).use { cursor ->

            val nameIndex =
                cursor.getColumnIndexOrThrow("name")

            while (cursor.moveToNext()) {
                columns +=
                    cursor.getString(nameIndex)
            }
        }

        return columns
    }
}