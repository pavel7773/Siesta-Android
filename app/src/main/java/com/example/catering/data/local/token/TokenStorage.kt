package com.example.catering.data.local.token

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.security.GeneralSecurityException
import javax.inject.Inject

internal val Context.tokenDataStore by preferencesDataStore(
    name = "saveDataToken"
)

// Старый ключ нужен только для безопасной миграции
// уже установленных версий приложения.
private val LEGACY_TOKEN_DATA_KEY =
    stringPreferencesKey("KeyDataToken")

private val ENCRYPTED_TOKEN_DATA_KEY =
    stringPreferencesKey("siesta_session_token_ciphertext_v1")

private val TOKEN_IV_DATA_KEY =
    stringPreferencesKey("siesta_session_token_iv_v1")

class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: TokenCryptoManager
) {

    suspend fun saveToken(token: String) {
        require(token.isNotBlank()) {
            "Токен авторизации не должен быть пустым"
        }

        val encrypted = cryptoManager.encrypt(
            token.toByteArray(Charsets.UTF_8)
        )

        val encodedToken = Base64.encodeToString(
            encrypted.ciphertext,
            Base64.NO_WRAP
        )

        val encodedIv = Base64.encodeToString(
            encrypted.iv,
            Base64.NO_WRAP
        )

        context.tokenDataStore.edit { preferences ->
            preferences[ENCRYPTED_TOKEN_DATA_KEY] =
                encodedToken

            preferences[TOKEN_IV_DATA_KEY] =
                encodedIv

            // Если пользователь обновился со старой версии,
            // plaintext JWT больше не оставляем.
            preferences.remove(LEGACY_TOKEN_DATA_KEY)
        }
    }

    suspend fun readToken(): String? {
        val preferences =
            context.tokenDataStore.data.first()

        val encodedToken =
            preferences[ENCRYPTED_TOKEN_DATA_KEY]

        val encodedIv =
            preferences[TOKEN_IV_DATA_KEY]

        // Есть хотя бы часть новой записи.
        if (
            encodedToken != null ||
            encodedIv != null
        ) {
            if (
                encodedToken.isNullOrBlank() ||
                encodedIv.isNullOrBlank()
            ) {
                clearToken()
                return null
            }

            return try {
                val encryptedData =
                    EncryptedTokenData(
                        ciphertext = Base64.decode(
                            encodedToken,
                            Base64.NO_WRAP
                        ),
                        iv = Base64.decode(
                            encodedIv,
                            Base64.NO_WRAP
                        )
                    )

                cryptoManager.decrypt(
                    encryptedData
                ).toString(Charsets.UTF_8)

            } catch (_: GeneralSecurityException) {
                clearToken()
                null

            } catch (_: IllegalArgumentException) {
                clearToken()
                null

            } catch (_: IllegalStateException) {
                clearToken()
                null
            }
        }

        // Миграция со старого plaintext DataStore.
        val legacyToken =
            preferences[LEGACY_TOKEN_DATA_KEY]

        if (legacyToken.isNullOrBlank()) {
            return null
        }

        saveToken(legacyToken)

        return legacyToken
    }

    suspend fun hasStoredToken(): Boolean {
        val preferences =
            context.tokenDataStore.data.first()

        return preferences[ENCRYPTED_TOKEN_DATA_KEY] != null ||
                preferences[TOKEN_IV_DATA_KEY] != null ||
                preferences[LEGACY_TOKEN_DATA_KEY] != null
    }

    suspend fun clearToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(
                ENCRYPTED_TOKEN_DATA_KEY
            )
            preferences.remove(
                TOKEN_IV_DATA_KEY
            )
            preferences.remove(
                LEGACY_TOKEN_DATA_KEY
            )
        }

        cryptoManager.deleteKey()
    }
}