
package com.example.catering.domain.repository

import com.example.catering.domain.model.RegistrationPending
import com.example.catering.domain.model.User

interface UserRepository {
    suspend fun getEmailStatus(): com.example.catering.domain.model.EmailStatus

    suspend fun saveUser(user: User)

    suspend fun restoreUser(): User?

    suspend fun loginUser(
        phone: String,
        password: String
    ): User

    suspend fun registerUser(
        name: String,
        phone: String,
        password: String
    ): RegistrationPending

    suspend fun verifyRegistrationPhone(
        phone: String,
        code: String
    ): User

    suspend fun resendRegistrationPhoneCode(
        phone: String
    )

    suspend fun clearToken()

    suspend fun getToken(): String?

    suspend fun getCurrentUser(): User

    suspend fun updateProfile(
        name: String? = null,
        address: String? = null
    ): User

    suspend fun clearSavedUser()

    // Смена пароля авторизованного пользователя.
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    )

    // Добавление email в профиль.
    suspend fun saveEmail(email: String)

    // Отправка кода подтверждения email.
    suspend fun sendEmailVerificationCode()

    // Подтверждение email.
    suspend fun verifyEmail(code: String)

    // Запрос восстановления пароля.
    suspend fun forgotPassword(email: String)

    // Установка нового пароля по коду.
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    )
}
