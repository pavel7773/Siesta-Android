package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


sealed interface UpdateUserNameResult {
    data class Success(val user: User) : UpdateUserNameResult
    data class Error(val message: String) : UpdateUserNameResult
}

class UpdateUserNameUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend fun execute(
        user: User?,
        name: String
    ): UpdateUserNameResult {

        if (user == null) {
            return UpdateUserNameResult.Error("Пользователь не найден")
        }
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            return UpdateUserNameResult.Error("Введите имя")
        }
        return try {

            val updatedUser =
                userRepository.updateProfile(
                    name = cleanName
                )

            UpdateUserNameResult.Success(
                updatedUser
            )

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            UpdateUserNameResult.Error(
                (e as? UserException)?.message ?: "Не удалось сохранить имя пользователя"
            )
        }
    }
}
