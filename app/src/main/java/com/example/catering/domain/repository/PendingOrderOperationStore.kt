package com.example.catering.domain.repository

import com.example.catering.domain.model.PendingOrderOperation

interface PendingOrderOperationStore {

    suspend fun load(): PendingOrderOperation?

    suspend fun save(operation: PendingOrderOperation)

    suspend fun clear()
}