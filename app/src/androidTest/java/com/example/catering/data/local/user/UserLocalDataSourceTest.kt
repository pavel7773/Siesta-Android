package com.example.catering.data.local.user

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catering.domain.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var dataSource: UserLocalDataSource

    @Before
    fun setup() {

        context =
            ApplicationProvider.getApplicationContext()

        dataSource =
            UserLocalDataSource(context)

        // Каждый тест начинает работу
        // с полностью чистого DataStore.
        runBlocking {
            context.userDataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    @After
    fun tearDown() {

        runBlocking {
            context.userDataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    @Test
    fun saveUser_andRestoreUser_returnsSameUser() = runTest {

        val user = User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        dataSource.saveUser(user)

        val restoredUser =
            dataSource.restoreUser()

        assertEquals(
            user,
            restoredUser
        )
    }

    @Test
    fun restoreUser_returnsNullWhenUserIsNotSaved() = runTest {

        val result =
            dataSource.restoreUser()

        assertNull(result)
    }

    @Test
    fun restoreUser_returnsNullWhenSavedJsonIsCorrupted() = runTest {

        // Специально записываем повреждённый JSON.
        saveUserJsonToDataStore(
            context = context,
            saveString = "{ definitely-not-valid-json"
        )

        val result =
            dataSource.restoreUser()

        // Повреждённые локальные данные
        // не должны приводить к crash приложения.
        assertNull(result)
    }
    @Test
    fun clearUser_removesSavedUser() = runTest {

        val user = User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        dataSource.saveUser(user)

        val beforeClear =
            dataSource.restoreUser()

        assertEquals(
            user,
            beforeClear
        )

        dataSource.clearUser()

        val afterClear =
            dataSource.restoreUser()

        assertNull(
            afterClear
        )
    }
}