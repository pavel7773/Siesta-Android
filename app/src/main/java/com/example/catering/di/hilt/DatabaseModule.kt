package com.example.catering.di.hilt

import android.content.Context
import com.example.catering.data.local.cart.CartItemDao
import com.example.catering.data.local.database.AppDatabase
import com.example.catering.data.local.database.DatabaseProvider
import com.example.catering.data.local.favorite.FavoriteDishDao
import com.example.catering.data.local.menu.DishDao
import com.example.catering.data.local.menu.MenuCacheInfoDao
import com.example.catering.data.local.order.OrderDao
import com.example.catering.data.local.order.OrderItemDao
import com.example.catering.data.local.recently.RecentlyViewedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return DatabaseProvider.getDatabase(context)
    }

    @Provides
    fun provideDishDao(
        appDatabase: AppDatabase
    ): DishDao {
        return appDatabase.dishDao()
    }

    @Provides
    fun provideMenuCacheInfoDao(
        appDatabase: AppDatabase
    ): MenuCacheInfoDao {
        return appDatabase.menuCacheInfoDao()
    }

    @Provides
    fun provideCartItemDao(
        appDatabase: AppDatabase
    ): CartItemDao {
        return appDatabase.cartItemDao()
    }

    @Provides
    fun provideFavoriteDao(
        appDatabase: AppDatabase
    ): FavoriteDishDao {
        return appDatabase.favoriteDishDao()
    }

    @Provides
    fun provideOrderDao(
        appDatabase: AppDatabase
    ): OrderDao {
        return appDatabase.orderDao()
    }

    @Provides
    fun provideOrderItemDao(
        appDatabase: AppDatabase
    ): OrderItemDao {
        return appDatabase.orderItemDao()
    }

    @Provides
    fun provideRecentlyDao(
        appDatabase: AppDatabase
    ): RecentlyViewedDao {
        return appDatabase.recentlyViewedDao()
    }
}
