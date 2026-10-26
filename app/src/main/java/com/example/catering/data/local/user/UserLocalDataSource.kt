package com.example.catering.data.local.user

import android.content.Context
import com.example.catering.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun saveUser(user: User) {

        val savedUser =
            userToSavedUser(user)

        val json =
            savedUserToJson(savedUser)

        saveUserJsonToDataStore(
            context = context,
            saveString = json
        )
    }

    suspend fun restoreUser(): User? {

        val savedJson =
            readUserJsonFromDataStore(context)
                ?: return null

        return try {

            val savedUser =
                jsonToSavedUser(savedJson)

            savedUserToUser(savedUser)

        } catch (e: CancellationException) {

            // Отмену coroutine никогда не превращаем
            // в обычный результат.
            throw e

        } catch (e: SerializationException) {

            // Повреждённый JSON не должен ронять приложение.
            // Считаем, что сохранённого пользователя нет.
            null
        }
    }
    suspend fun clearUser() {
        clearUserFromDataStore(context)
    }
}