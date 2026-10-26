package com.example.catering.data.local.order

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.catering.domain.model.PendingOrderOperation
import com.example.catering.domain.repository.PendingOrderOperationStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pendingOrderDataStore by preferencesDataStore(
    name = "pending_order_operation"
)

private val IDEMPOTENCY_KEY =
    stringPreferencesKey("idempotency_key")

private val ORDER_FINGERPRINT =
    stringPreferencesKey("order_fingerprint")

private val USER_ID =
    intPreferencesKey("user_id")

@Singleton
class PendingOrderDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : PendingOrderOperationStore {

    override suspend fun load(): PendingOrderOperation? {
        val preferences =
            context.pendingOrderDataStore.data.first()

        val key =
            preferences[IDEMPOTENCY_KEY]
                ?: return null

        val fingerprint =
            preferences[ORDER_FINGERPRINT]
                ?: return null

        val userId =
            preferences[USER_ID]
                ?: return null

        return PendingOrderOperation(
            idempotencyKey = key,
            orderFingerprint = fingerprint,
            userId = userId
        )
    }

    override suspend fun save(
        operation: PendingOrderOperation
    ) {
        context.pendingOrderDataStore.edit { preferences ->
            preferences[IDEMPOTENCY_KEY] =
                operation.idempotencyKey

            preferences[ORDER_FINGERPRINT] =
                operation.orderFingerprint

            preferences[USER_ID] =
                operation.userId
        }
    }

    override suspend fun clear() {
        context.pendingOrderDataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(IDEMPOTENCY_KEY)
            preferences.remove(ORDER_FINGERPRINT)
        }
    }
}