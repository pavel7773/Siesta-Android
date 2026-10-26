package com.example.catering.data.local.user

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.catering.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

val Context.userDataStore by preferencesDataStore(name = "saveDataUser") // получение доступа к дата сторе
val USER_DATA_KEY = stringPreferencesKey(name = "KeyDataUser") // создание ключа для пользователя



suspend fun saveUserJsonToDataStore(context: Context, saveString: String) {// записали строку
    context.userDataStore.edit { preferences ->
        preferences[USER_DATA_KEY] = saveString
    }
}

suspend fun readUserJsonFromDataStore(context: Context): String? {// чтение строки
    val preferences = context.userDataStore.data.first()
    return preferences[USER_DATA_KEY]
}
fun userToSavedUser(user: User): SavedUser {// преобразование элемента
    val id = user.id
    val name = user.name
    val phone = user.phone
    val address = user.address
    return SavedUser(id,name, phone, address)
}

fun savedUserToUser(savedUser: SavedUser): User { // преобразования сохраненого пользователя
    val id = savedUser.id
    val name = savedUser.name
    val phone = savedUser.phone
    val address = savedUser.address
    return User(id, name, phone, address)
}

fun savedUserToJson(user: SavedUser): String { // пользователь → JSON
    val json = Json.encodeToString(user)
    return json
}

fun jsonToSavedUser(user: String): SavedUser{ // JSON → пользователь
    val savedUserItem = Json.decodeFromString<SavedUser>(user)
    return savedUserItem
}

suspend fun clearUserFromDataStore(
    context: Context
) {
    context.userDataStore.edit { preferences ->
        preferences.remove(USER_DATA_KEY)
    }
}