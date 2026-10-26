package com.example.catering.di.hilt

import com.example.catering.data.repository.cart.CartRoomRepository
import com.example.catering.data.repository.favorite.FavoriteRoomRepository
import com.example.catering.data.repository.menu.MenuRepositoryImpl
import com.example.catering.data.repository.order.OrderRoomRepository
import com.example.catering.data.repository.recently.RecentlyViewedRoomRepository
import com.example.catering.data.repository.user.UserRepositoryImpl
import com.example.catering.domain.repository.CartRepository
import com.example.catering.domain.repository.FavoriteRepository
import com.example.catering.domain.repository.MenuRepository
import com.example.catering.domain.repository.OrderRepository
import com.example.catering.domain.repository.RecentlyViewedRepository
import com.example.catering.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        menuRepositoryImpl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRoomRepository: CartRoomRepository
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRoomRepository: FavoriteRoomRepository
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRoomRepository: OrderRoomRepository
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindRecentlyRepository(
        recentlyViewedRoomRepository: RecentlyViewedRoomRepository
    ): RecentlyViewedRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}