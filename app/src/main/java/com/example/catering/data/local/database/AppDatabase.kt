package com.example.catering.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.catering.data.local.cart.CartItemDao
import com.example.catering.data.local.cart.CartItemEntity
import com.example.catering.data.local.favorite.FavoriteDishDao
import com.example.catering.data.local.favorite.FavoriteDishEntity
import com.example.catering.data.local.menu.DishDao
import com.example.catering.data.local.menu.DishEntity
import com.example.catering.data.local.menu.MenuCacheInfoDao
import com.example.catering.data.local.menu.MenuCacheInfoEntity
import com.example.catering.data.local.order.OrderDao
import com.example.catering.data.local.order.OrderEntity
import com.example.catering.data.local.order.OrderItemDao
import com.example.catering.data.local.order.OrderItemEntity
import com.example.catering.data.local.recently.RecentlyViewedDao
import com.example.catering.data.local.recently.RecentlyViewedDishEntity

@Database(
    entities = [
        FavoriteDishEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        RecentlyViewedDishEntity::class,
        DishEntity::class,
        MenuCacheInfoEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase :
    RoomDatabase() {
    abstract fun favoriteDishDao(): FavoriteDishDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao
    abstract fun dishDao(): DishDao
    abstract fun menuCacheInfoDao(): MenuCacheInfoDao

}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE order_history ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE order_history ADD COLUMN statusOrder TEXT NOT NULL DEFAULT 'ACCEPTED'"
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS menu_dishes (" +
                    "    id INTEGER NOT NULL PRIMARY KEY," +
                    "    name TEXT NOT NULL," +
                    "    price INTEGER NOT NULL," +
                    "    info TEXT NOT NULL," +
                    "    imageUrl TEXT NOT NULL," +
                    "    weight INTEGER NOT NULL," +
                    "    calories INTEGER NOT NULL," +
                    "    proteins REAL NOT NULL," +
                    "    fats REAL NOT NULL," +
                    "    carbs REAL NOT NULL," +
                    "    category TEXT NOT NULL" +
                    ")"
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS menu_cache_info (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    "lastUpdatedAtMillis INTEGER NOT NULL" +
                    ")"
        )
    }
}