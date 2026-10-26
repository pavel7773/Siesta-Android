package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface RestoreSessionResult {
    data class Success(
        val currentUser: User?
    ) : RestoreSessionResult

    data class Error(val message: String) : RestoreSessionResult
}

class RestoreSessionUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend fun execute(): RestoreSessionResult {
        return try {
            val token = userRepository.getToken()
            if (token == null) {
                return RestoreSessionResult.Success(
                    currentUser = null
                )
            }

            try {

                val user = userRepository.getCurrentUser()

                userRepository.saveUser(user)

                RestoreSessionResult.Success(
                    currentUser = user
                )

            } catch (e: UserException.SessionExpired) {

                userRepository.clearToken()

                RestoreSessionResult.Success(
                    currentUser = null
                )

            } catch (e: UserException.Network) {

                val savedUser = userRepository.restoreUser()

                RestoreSessionResult.Success(
                    currentUser = savedUser
                )

            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: UserException) {

            RestoreSessionResult.Error(
                e.message ?: "Не удалось проверить сессию"
            )

        } catch (e: Exception) {

            RestoreSessionResult.Error(
                "Не удалось восстановить пользователя. Попробуйте снова"
            )
        }
    }
}
