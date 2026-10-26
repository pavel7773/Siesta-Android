package com.example.catering.data.local.order

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catering.data.local.database.AppDatabase
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderLocalDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var dataSource: OrderLocalDataSource

    @Before
    fun setup() {

        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dataSource = OrderLocalDataSource(
            orderDao = database.orderDao(),
            orderItemDao = database.orderItemDao(),
            database = database
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveOrder_savesOrderAndItsItems() = runTest {

        val order = createOrder(
            id = 42,
            name = "Павел",
            createdAt = 1000L,
            status = OrderStatus.ACCEPTED
        )

        dataSource.saveOrder(order)

        val savedOrders =
            dataSource
                .getAllOrdersFlow()
                .first()

        assertEquals(
            1,
            savedOrders.size
        )

        val savedOrder =
            savedOrders.single()

        assertEquals(
            42,
            savedOrder.id
        )

        assertEquals(
            "Павел",
            savedOrder.name
        )

        assertEquals(
            2,
            savedOrder.products.size
        )

        assertEquals(
            listOf(10, 20),
            savedOrder.products
                .map { it.dish.id }
                .sorted()
        )

        assertEquals(
            listOf(2, 3),
            savedOrder.products
                .map { it.quantity }
                .sorted()
        )

        assertEquals(
            OrderStatus.ACCEPTED,
            savedOrder.statusOrder
        )
    }

    @Test
    fun saveOrder_mapsStoredItemsBackToDomainOrder() = runTest {

        val order = createOrder(
            id = 7,
            name = "Иван",
            createdAt = 2000L,
            status = OrderStatus.ACCEPTED
        )

        dataSource.saveOrder(order)

        val result =
            dataSource
                .getAllOrdersFlow()
                .first()
                .single()

        assertEquals(
            order.id,
            result.id
        )

        assertEquals(
            order.name,
            result.name
        )

        assertEquals(
            order.phone,
            result.phone
        )

        assertEquals(
            order.comment,
            result.comment
        )

        assertEquals(
            order.deliveryType,
            result.deliveryType
        )

        assertEquals(
            order.address,
            result.address
        )

        assertEquals(
            order.totalAmount,
            result.totalAmount
        )

        assertEquals(
            order.createdAt,
            result.createdAt
        )

        assertEquals(
            order.statusOrder,
            result.statusOrder
        )

        // OrderItemEntity хранит только часть Dish.
        // Поэтому проверяем именно те поля,
        // которые реально сохраняются в order_item_history.
        assertEquals(
            order.products.map { it.dish.id }.sorted(),
            result.products.map { it.dish.id }.sorted()
        )

        assertEquals(
            order.products.map { it.dish.name }.sorted(),
            result.products.map { it.dish.name }.sorted()
        )

        assertEquals(
            order.products.map { it.dish.price }.sorted(),
            result.products.map { it.dish.price }.sorted()
        )

        assertEquals(
            order.products.map { it.quantity }.sorted(),
            result.products.map { it.quantity }.sorted()
        )
    }

    @Test
    fun updateOrderStatus_updatesStoredOrder() = runTest {

        val order = createOrder(
            id = 42,
            name = "Павел",
            createdAt = 1000L,
            status = OrderStatus.ACCEPTED
        )

        dataSource.saveOrder(order)

        dataSource.updateOrderStatus(
            orderId = 42,
            newStatus = OrderStatus.CANCELED.name
        )

        val updatedOrder =
            dataSource
                .getAllOrdersFlow()
                .first()
                .single()

        assertEquals(
            OrderStatus.CANCELED,
            updatedOrder.statusOrder
        )
    }

    @Test
    fun replaceOrderHistory_removesOldOrdersAndStoresNewHistory() = runTest {

        val oldOrder = createOrder(
            id = 1,
            name = "Старый заказ",
            createdAt = 1000L
        )

        dataSource.saveOrder(oldOrder)

        val newOrder1 = createOrder(
            id = 10,
            name = "Новый заказ 1",
            createdAt = 3000L
        )

        val newOrder2 = createOrder(
            id = 20,
            name = "Новый заказ 2",
            createdAt = 4000L
        )

        dataSource.replaceOrderHistory(
            listOf(
                newOrder1,
                newOrder2
            )
        )

        val history =
            dataSource
                .getAllOrdersFlow()
                .first()

        assertEquals(
            2,
            history.size
        )

        assertEquals(
            setOf(10, 20),
            history.map { it.id }.toSet()
        )

        assertTrue(
            history.none { it.id == 1 }
        )
    }

    @Test
    fun replaceOrderHistory_preservesOrderItemsForEachOrder() = runTest {

        val firstOrder = createOrder(
            id = 10,
            name = "Первый",
            createdAt = 1000L
        )

        val secondOrder = createOrder(
            id = 20,
            name = "Второй",
            createdAt = 2000L
        )

        dataSource.replaceOrderHistory(
            listOf(
                firstOrder,
                secondOrder
            )
        )

        val history =
            dataSource
                .getAllOrdersFlow()
                .first()

        val firstSaved =
            history.first { it.id == 10 }

        val secondSaved =
            history.first { it.id == 20 }

        assertEquals(
            setOf(10, 20),
            firstSaved.products
                .map { it.dish.id }
                .toSet()
        )

        assertEquals(
            setOf(10, 20),
            secondSaved.products
                .map { it.dish.id }
                .toSet()
        )
    }

    @Test
    fun unknownStoredStatus_fallsBackToAccepted() = runTest {

        database.orderDao().insertOrder(
            OrderEntity(
                id = 50,
                name = "Тест",
                phone = "79990000000",
                comment = "",
                deliveryType = "Доставка",
                address = "Улица 1",
                totalAmount = 1000,
                createdAt = 1000L,
                statusOrder = "SOME_UNKNOWN_STATUS"
            )
        )

        val result =
            dataSource
                .getAllOrdersFlow()
                .first()
                .single()

        assertEquals(
            OrderStatus.ACCEPTED,
            result.statusOrder
        )
    }

    @Test
    fun replaceOrderHistory_rollsBackWhenOrderItemInsertFails() = runTest {

        val oldOrder = createOrder(
            id = 1,
            name = "Старая история",
            createdAt = 1000L
        )

        dataSource.saveOrder(oldOrder)

        // Создаём SQLite trigger, который специально ломает INSERT
        // позиции с dishId = 999.
        //
        // Нам это нужно только для теста rollback.
        database.openHelper
            .writableDatabase
            .execSQL(
                """
                CREATE TRIGGER fail_order_item_insert
                BEFORE INSERT ON order_item_history
                WHEN NEW.dishId = 999
                BEGIN
                    SELECT RAISE(ABORT, 'forced test failure');
                END
                """.trimIndent()
            )

        val brokenOrder =
            createOrder(
                id = 100,
                name = "Сломанный новый заказ",
                createdAt = 5000L,
                firstDishId = 999
            )

        val result = runCatching {

            dataSource.replaceOrderHistory(
                listOf(brokenOrder)
            )
        }

        assertTrue(
            result.isFailure
        )

        val historyAfterFailure =
            dataSource
                .getAllOrdersFlow()
                .first()

        // Ключевая проверка:
        // clearOrderItems() и clearOrders() уже успели выполниться,
        // но последующий INSERT упал.
        //
        // Так как всё находится внутри withTransaction(),
        // Room должен откатить и DELETE тоже.
        assertEquals(
            1,
            historyAfterFailure.size
        )

        val preservedOrder =
            historyAfterFailure.single()

        assertEquals(
            1,
            preservedOrder.id
        )

        assertEquals(
            "Старая история",
            preservedOrder.name
        )

        // Проверяем ещё и дочерние записи.
        // Старые OrderItemEntity тоже должны восстановиться
        // после rollback.
        assertEquals(
            setOf(10, 20),
            preservedOrder.products
                .map { it.dish.id }
                .toSet()
        )
    }

    private fun createOrder(
        id: Int,
        name: String,
        createdAt: Long,
        status: OrderStatus = OrderStatus.ACCEPTED,
        firstDishId: Int = 10
    ): Order {

        val firstDish = Dish(
            id = firstDishId,
            name = "Паста",
            price = 500,
            info = "Полное описание",
            imageUrl = "image1",
            weight = 300,
            calories = 500,
            proteins = 20.0,
            fats = 15.0,
            carbs = 60.0,
            category = "Горячее"
        )

        val secondDish = Dish(
            id = 20,
            name = "Пицца",
            price = 700,
            info = "Полное описание",
            imageUrl = "image2",
            weight = 400,
            calories = 700,
            proteins = 25.0,
            fats = 20.0,
            carbs = 80.0,
            category = "Горячее"
        )

        return Order(
            id = id,
            name = name,
            phone = "79991234567",
            comment = "Без лука",
            deliveryType = "Доставка",
            address = "Улица 1",
            products = listOf(
                CartItem(
                    dish = firstDish,
                    quantity = 2
                ),
                CartItem(
                    dish = secondDish,
                    quantity = 3
                )
            ),
            totalAmount = 3100,
            createdAt = createdAt,
            statusOrder = status
        )
    }

    @Test
    fun clearOrderHistory_removesOrdersAndTheirItems() =
        runTest {

            val order = createOrder(
                id = 100,
                name = "Заказ для очистки",
                createdAt = 1000L
            )

            dataSource.saveOrder(order)

            val beforeClear =
                dataSource
                    .getAllOrdersFlow()
                    .first()

            assertEquals(
                1,
                beforeClear.size
            )

            val itemsBeforeClear =
                database.openHelper
                    .readableDatabase
                    .query(
                        "SELECT COUNT(*) FROM order_item_history"
                    )
                    .use { cursor ->

                        cursor.moveToFirst()
                        cursor.getInt(0)
                    }

            assertTrue(
                itemsBeforeClear > 0
            )

            dataSource.clearOrderHistory()

            val itemsAfterClear =
                database.openHelper
                    .readableDatabase
                    .query(
                        "SELECT COUNT(*) FROM order_item_history"
                    )
                    .use { cursor ->

                        cursor.moveToFirst()
                        cursor.getInt(0)
                    }

            assertEquals(
                0,
                itemsAfterClear
            )

            val afterClear =
                dataSource
                    .getAllOrdersFlow()
                    .first()

            assertTrue(
                afterClear.isEmpty()
            )
        }
}