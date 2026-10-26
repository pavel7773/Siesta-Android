package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface RegisterUserResult {
    data class Success(val user: User) : RegisterUserResult
    data class Error(val message: String) : RegisterUserResult
}

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend fun execute(
        name: String,
        phone: String,
        password: String,
        passwordRepeat: String
    ): RegisterUserResult {
        val cleanName = name.trim()
        val cleanPhone = phone.trim()

        val validationError = validate(
            name = cleanName,
            phone = cleanPhone,
            password = password,
            passwordRepeat = passwordRepeat
        )

        if (validationError != null) {
            return RegisterUserResult.Error(validationError)
        }

        return try {

            val newUser = userRepository.registerUser(
                name = cleanName,
                phone = cleanPhone,
                password = password
            )

            userRepository.saveUser(newUser)

            RegisterUserResult.Success(newUser)

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            RegisterUserResult.Error(
                (e as? UserException)?.message ?: "Не удалось зарегистрировать пользователя"
            )
        }
    }

    private fun validate(
        name: String,
        phone: String,
        password: String,
        passwordRepeat: String
    ): String? {
        if (name.isBlank()) {
            return "Введите имя"
        } else if (phone.isBlank() || phone.length < 10) {
            return "Введите корректный номер телефона"
        } else if (password.length < 6) {
            return "Введите пароль больше 6 символов"
        } else if (password != passwordRepeat) {
            return "Пароли не совпадают"
        }

        return null
    }
}
