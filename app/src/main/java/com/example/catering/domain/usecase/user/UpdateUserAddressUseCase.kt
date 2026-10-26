package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface UpdateUserAddressResult {
    data class Success(val user: User) : UpdateUserAddressResult
    data class Error(val message: String) : UpdateUserAddressResult
}

class UpdateUserAddressUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend fun execute(
        user: User?,
        address: String
    ): UpdateUserAddressResult {
        if (user == null) {
            return UpdateUserAddressResult.Error("Пользователь не найден")
        }

        return try {

            val updatedUser =
                userRepository.updateProfile(
                    address = address.trim()
                )

            UpdateUserAddressResult.Success(
                updatedUser
            )

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            UpdateUserAddressResult.Error(
                (e as? UserException)?.message ?: "Не удалось сохранить адрес"
            )
        }
    }
}
