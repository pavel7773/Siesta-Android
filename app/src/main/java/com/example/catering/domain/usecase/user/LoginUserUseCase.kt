package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface LoginUserResult {
    data class Success(val user: User) : LoginUserResult
    data class Error(val message: String) : LoginUserResult
}

class LoginUserUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend fun execute(phone: String, password: String): LoginUserResult {
        if (phone.trim().isBlank() || phone.trim().length < 10) {
            return LoginUserResult.Error("Введите корректный номер телефона")
        }

        if (password.length < 6) {
            return LoginUserResult.Error("Введите пароль больше 6 символов")
        }

        return try {
            val user = userRepository.loginUser(
                phone = phone.trim(),
                password = password
            )

            userRepository.saveUser(user)

            LoginUserResult.Success(user)

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            LoginUserResult.Error(
                (e as? UserException)?.message ?: "Не удалось выполнить вход"
            )
        }
    }
}
