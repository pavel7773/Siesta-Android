
package com.example.catering.data.local.biometric

import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import java.util.concurrent.CancellationException
import javax.crypto.Cipher
import javax.inject.Inject

sealed interface BiometricRestoreResult {

    data class Success(
        val user: User
    ) : BiometricRestoreResult

    data class Error(
        val message: String
    ) : BiometricRestoreResult
}

class BiometricSessionRestorer @Inject constructor(
    private val sessionManager: BiometricSessionManager,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider
) {

    suspend fun execute(
        authenticatedCipher: Cipher
    ): BiometricRestoreResult {

        // Убираем возможный старый JWT из памяти.
        tokenProvider.clearToken()

        return try {

            // Расшифровываем JWT после успешного
            // подтверждения отпечатка.
            sessionManager.completeUnlock(
                authenticatedCipher
            )

            // Обязательно проверяем JWT на сервере.
            // Проверяется и актуальность session_version.
            val user = userRepository.getCurrentUser()

            // Сохраняем только подтверждённого пользователя.
            userRepository.saveUser(user)

            BiometricRestoreResult.Success(user)

        } catch (e: CancellationException) {
            tokenProvider.clearToken()
            throw e

        } catch (e: UserException.SessionExpired) {

            tokenProvider.clearToken()

            BiometricRestoreResult.Error(
                "Сессия истекла. Войдите по паролю"
            )

        } catch (e: UserException.Network) {

            tokenProvider.clearToken()

            BiometricRestoreResult.Error(
                "Не удалось проверить аккаунт. Проверьте интернет"
            )

        } catch (e: Exception) {

            tokenProvider.clearToken()

            BiometricRestoreResult.Error(
                "Не удалось выполнить биометрический вход"
            )
        }
    }
}
