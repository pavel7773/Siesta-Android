
package com.example.catering.data.local.biometric

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.repository.UserRepository
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricSessionManager @Inject constructor(
    private val cryptoManager: BiometricCryptoManager,
    private val tokenStore: BiometricTokenStore,
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository
) {

    suspend fun isBiometricEnabled(): Boolean {
        return tokenStore.hasProtectedSession()
    }

    suspend fun getProtectedSessionVersion(): Int {
        return tokenStore.getProtectedKeyVersion()
    }

    // Первоначальное включение защиты V1.
    fun prepareEnableCipher(): Cipher {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ) {
            cryptoManager.createCredentialEncryptionCipher()
        } else {
            cryptoManager.createEncryptionCipher()
        }
    }

    suspend fun completeEnable(
        authenticatedCipher: Cipher
    ) {
        val keyVersion =
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            ) {
                2
            } else {
                1
            }

        tokenStore.enableBiometric(
            authenticatedCipher = authenticatedCipher,
            keyVersion = keyVersion
        )
    }

    // Подготовка разблокировки с учётом версии ключа.
    suspend fun prepareUnlockCipher(): Cipher {
        val encrypted = tokenStore.getEncryptedData()

        return when (tokenStore.getProtectedKeyVersion()) {

            1 -> cryptoManager.createDecryptionCipher(
                iv = encrypted.iv
            )

            2 -> cryptoManager.createCredentialDecryptionCipher(
                iv = encrypted.iv
            )

            else -> error(
                "Неизвестная версия защищённой сессии"
            )
        }
    }

    // Вызывается после подтверждения Android.
    suspend fun completeUnlock(
        authenticatedCipher: Cipher
    ) {
        val token = tokenStore.unlockToken(
            authenticatedCipher
        )

        check(token.isNotBlank()) {
            "Не удалось восстановить токен"
        }

        // Только оперативная память.
        tokenProvider.setToken(token)
    }

    // Подготовка переноса V1 -> V2.
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun prepareCredentialMigrationCipher(): Cipher {
        check(tokenStore.getProtectedKeyVersion() == 1) {
            "Перенос не требуется"
        }

        check(!tokenProvider.getToken().isNullOrBlank()) {
            "Сначала разблокируйте текущую сессию"
        }

        return cryptoManager.createCredentialEncryptionCipher()
    }

    // Вызывать после успешного BiometricPrompt
    // с Cipher нового ключа V2.
    suspend fun completeCredentialMigration(
        authenticatedCipher: Cipher
    ) {
        check(tokenStore.getProtectedKeyVersion() == 1) {
            "Защищённая сессия уже обновлена"
        }

        val token = tokenProvider.getToken()
            ?.takeIf { it.isNotBlank() }
            ?: error("Нет разблокированной сессии")

        // Дополнительно проверяем JWT на сервере.
        // Недействительный токен не переносим.
        userRepository.getCurrentUser()

        check(tokenProvider.getToken() == token) {
            "Сессия изменилась во время переноса"
        }

        // Сохраняем токен, IV и версию V2
        // одной транзакцией DataStore.
        tokenStore.migrateToCredentialProtection(
            authenticatedCipher = authenticatedCipher,
            verifiedToken = token
        )
    }
    suspend fun isEnableOfferDismissed(): Boolean {
        return tokenStore.isEnableOfferDismissed()
    }

    suspend fun dismissEnableOffer() {
        tokenStore.dismissEnableOffer()
    }
}
