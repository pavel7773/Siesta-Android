package com.example.catering.domain.usecase.password

import com.example.catering.domain.repository.UserRepository
import java.util.Locale
import javax.inject.Inject

class PasswordRecoveryUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    private val emailRegex =
        Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

    private val codeRegex = Regex("""\d{6}""")

    suspend fun requestCode(email: String) {
        val normalizedEmail = email.trim()
            .lowercase(Locale.ROOT)

        require(
            normalizedEmail.length in 5..254 &&
                    emailRegex.matches(normalizedEmail)
        ) {
            "Введите корректный email"
        }

        userRepository.forgotPassword(normalizedEmail)
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        val normalizedEmail = email.trim()
            .lowercase(Locale.ROOT)

        require(
            normalizedEmail.length in 5..254 &&
                    emailRegex.matches(normalizedEmail)
        ) {
            "Введите корректный email"
        }

        require(codeRegex.matches(code)) {
            "Код должен содержать 6 цифр"
        }

        require(newPassword.length in 6..128) {
            "Пароль должен содержать от 6 до 128 символов"
        }

        userRepository.resetPassword(
            email = normalizedEmail,
            code = code,
            newPassword = newPassword
        )
    }
}
