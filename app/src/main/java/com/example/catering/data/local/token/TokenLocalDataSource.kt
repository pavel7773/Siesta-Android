package com.example.catering.data.local.token

import com.example.catering.data.local.biometric.BiometricTokenStore
import javax.inject.Inject

class TokenLocalDataSource @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val biometricTokenStore: BiometricTokenStore
) {

    suspend fun saveToken(token: String) {

        // Нельзя создавать обычную сессию поверх
        // существующей биометрически защищённой.
        check(
            !biometricTokenStore.hasProtectedSession()
        ) {
            "Сначала необходимо завершить защищённую сессию"
        }

        tokenStorage.saveToken(token)
    }

    suspend fun getToken(): String? {

        // Защищённая сессия может быть восстановлена
        // только через BiometricPrompt.
        if (
            biometricTokenStore.hasProtectedSession()
        ) {
            return null
        }

        return tokenStorage.readToken()
    }

    suspend fun clearToken() {
        biometricTokenStore.clearAllTokens()
    }
}