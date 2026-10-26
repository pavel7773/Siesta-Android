package com.example.catering.domain.usecase.user

import com.example.catering.domain.repository.UserRepository
import java.util.Locale
import javax.inject.Inject

class EmailVerificationUseCase @Inject constructor(private val repository: UserRepository) {
    suspend fun status() = repository.getEmailStatus()

    suspend fun save(email: String) {
        val normalized = email.trim().lowercase(Locale.ROOT)
        require(normalized.length in 5..254 && Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""").matches(normalized)) {
            "Введите корректный email"
        }
        repository.saveEmail(normalized)
    }

    suspend fun sendCode() = repository.sendEmailVerificationCode()

    suspend fun verify(code: String) {
        require(Regex("[0-9]{6}").matches(code)) { "Введите шестизначный код" }
        repository.verifyEmail(code)
    }
}
