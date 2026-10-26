package com.example.catering.data.local.biometric

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.catering.data.local.token.TokenStorage
import com.example.catering.data.local.token.tokenDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricTokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: BiometricCryptoManager,
    private val tokenStorage: TokenStorage
) {

    companion object {
        private val ENCRYPTED_TOKEN_KEY =
            stringPreferencesKey("siesta_biometric_token_v1")

        private val ENCRYPTED_IV_KEY =
            stringPreferencesKey("siesta_biometric_iv_v1")

        private val TOKEN_KEY_VERSION =
            intPreferencesKey("siesta_biometric_key_version")

        private const val LEGACY_VERSION = 1
        private const val CREDENTIAL_VERSION = 2
    }

    // Проверяем наличие защищённой сессии.
    // Даже повреждённую или неполную запись
    // считаем защищённой, чтобы не обходить проверку.
    suspend fun hasProtectedSession(): Boolean {
        val preferences = context.tokenDataStore.data.first()

        return preferences[ENCRYPTED_TOKEN_KEY] != null ||
                preferences[ENCRYPTED_IV_KEY] != null ||
        preferences[TOKEN_KEY_VERSION] != null
    }

    // Вызывать только с Cipher после
    // успешного BiometricPrompt.
    suspend fun enableBiometric(
        authenticatedCipher: Cipher
    ) {
        val token = tokenStorage.readToken()
            ?: error("Нет активного токена авторизации")

        val encrypted =
            cryptoManager.encryptAuthenticated(
                cipher = authenticatedCipher,
                data = token.toByteArray(
                    Charsets.UTF_8
                )
            )

        context.tokenDataStore.edit { preferences ->

            check(
                preferences[ENCRYPTED_TOKEN_KEY] == null &&
                        preferences[ENCRYPTED_IV_KEY] == null &&
                        preferences[TOKEN_KEY_VERSION] == null
            ) {
                "Биометрическая сессия уже существует"
            }

            preferences[ENCRYPTED_TOKEN_KEY] =
                Base64.encodeToString(
                    encrypted.ciphertext,
                    Base64.NO_WRAP
                )

            preferences[ENCRYPTED_IV_KEY] =
                Base64.encodeToString(
                    encrypted.iv,
                    Base64.NO_WRAP
                )

            preferences[TOKEN_KEY_VERSION] =
                LEGACY_VERSION
        }

        // Биометрическая копия успешно создана.
        // Обычную зашифрованную сессию удаляем.
        tokenStorage.clearToken()
    }

    // Получаем зашифрованные данные и IV.
    suspend fun getEncryptedData(): EncryptedData {
        val preferences = context.tokenDataStore.data.first()

        val encryptedToken = preferences[ENCRYPTED_TOKEN_KEY]
        val encodedIv = preferences[ENCRYPTED_IV_KEY]

        check(
            !encryptedToken.isNullOrEmpty() &&
                    !encodedIv.isNullOrEmpty()
        ) {
            "Защищённая сессия отсутствует или повреждена"
        }

        return EncryptedData(
            ciphertext = Base64.decode(
                encryptedToken,
                Base64.NO_WRAP
            ),
            iv = Base64.decode(
                encodedIv,
                Base64.NO_WRAP
            )
        )
    }

    // Вызывать только после BiometricPrompt.
    suspend fun unlockToken(
        authenticatedCipher: Cipher
    ): String {
        val encrypted = getEncryptedData()

        val decrypted = cryptoManager.decryptAuthenticated(
            cipher = authenticatedCipher,
            data = encrypted.ciphertext
        )

        return decrypted.toString(Charsets.UTF_8)
    }


    suspend fun migrateToCredentialProtection(
        authenticatedCipher: Cipher,
        verifiedToken: String
    ) {
        require(verifiedToken.isNotBlank()) {
            "Нет подтверждённого токена для переноса"
        }
        check(!tokenStorage.hasStoredToken()) {
            "Обнаружена параллельная обычная сессия"
        }

        val encrypted = cryptoManager.encryptAuthenticated(
            cipher = authenticatedCipher,
            data = verifiedToken.toByteArray(Charsets.UTF_8)
        )

        val encodedToken = Base64.encodeToString(
            encrypted.ciphertext,
            Base64.NO_WRAP
        )

        val encodedIv = Base64.encodeToString(
            encrypted.iv,
            Base64.NO_WRAP
        )

        // Меняем токен, IV и версию одновременно.
        // При ошибке старая запись V1 сохраняется.
        context.tokenDataStore.edit { preferences ->

            val oldToken = preferences[ENCRYPTED_TOKEN_KEY]
            val oldIv = preferences[ENCRYPTED_IV_KEY]

            check(
                !oldToken.isNullOrBlank() &&
                        !oldIv.isNullOrBlank()
            ) {
                "Старая защищённая сессия отсутствует"
            }

            val currentVersion =
                preferences[TOKEN_KEY_VERSION]
                    ?: LEGACY_VERSION

            check(currentVersion == LEGACY_VERSION) {
                "Перенос возможен только с версии V1"
            }

            preferences[ENCRYPTED_TOKEN_KEY] = encodedToken
            preferences[ENCRYPTED_IV_KEY] = encodedIv
            preferences[TOKEN_KEY_VERSION] =
                CREDENTIAL_VERSION
        }
    }


    // Полная очистка токенов при выходе.
    suspend fun clearAllTokens() {

        tokenStorage.clearToken()

        context.tokenDataStore.edit { preferences ->
            preferences.remove(ENCRYPTED_TOKEN_KEY)
            preferences.remove(ENCRYPTED_IV_KEY)
            preferences.remove(TOKEN_KEY_VERSION)
        }

        cryptoManager.deleteKey()
    }

    suspend fun getProtectedKeyVersion(): Int {
        val preferences = context.tokenDataStore.data.first()

        check(
            !preferences[ENCRYPTED_TOKEN_KEY].isNullOrBlank() &&
                    !preferences[ENCRYPTED_IV_KEY].isNullOrBlank()
        ) {
            "Защищённая сессия отсутствует или повреждена"
        }

        val version = preferences[TOKEN_KEY_VERSION]
            ?: LEGACY_VERSION

        check(
            version == LEGACY_VERSION ||
                    version == CREDENTIAL_VERSION
        ) {
            "Неизвестная версия защищённой сессии"
        }

        return version
    }

}
