package com.example.catering.di.hilt

import com.example.catering.data.local.order.PendingOrderDataStore
import com.example.catering.domain.repository.PendingOrderOperationStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PendingOrderModule {

    @Binds
    @Singleton
    abstract fun bindPendingOrderOperationStore(
        implementation: PendingOrderDataStore
    ): PendingOrderOperationStore
}