package com.example.catering.data.local.order

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catering.domain.model.PendingOrderOperation
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingOrderDataStoreTest {

    private lateinit var dataStore:
            PendingOrderDataStore

    @Before
    fun setup() = runTest {

        dataStore = PendingOrderDataStore(
            ApplicationProvider.getApplicationContext()
        )

        dataStore.clear()
    }

    @After
    fun tearDown() = runTest {
        dataStore.clear()
    }

    @Test
    fun saveAndLoadRestoresPendingOrderOperation() =
        runTest {

            val operation =
                PendingOrderOperation(
                    userId = 1,
                    idempotencyKey = "test-key",
                    orderFingerprint = "test-fingerprint"
                )

            dataStore.save(operation)

            val restored =
                dataStore.load()

            assertEquals(
                operation,
                restored
            )
        }

    @Test
    fun newDataStoreInstanceRestoresSavedOperation() =
        runTest {

            val operation =
                PendingOrderOperation(
                    userId = 42,
                    idempotencyKey = "persistent-key",
                    orderFingerprint = "persistent-fingerprint"
                )

            dataStore.save(operation)

            // Новый объект data source.
            // Данные должны остаться в настоящем DataStore.
            val recreatedDataStore =
                PendingOrderDataStore(
                    ApplicationProvider.getApplicationContext()
                )

            assertEquals(
                operation,
                recreatedDataStore.load()
            )
        }

    @Test
    fun clearRemovesPendingOrderOperation() =
        runTest {

            dataStore.save(
                PendingOrderOperation(
                    userId = 1,
                    idempotencyKey = "test-key",
                    orderFingerprint = "test-fingerprint"
                )
            )

            dataStore.clear()

            assertNull(
                dataStore.load()
            )
        }
}