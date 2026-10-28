
package com.example.catering.data.remote.user

import com.example.catering.data.remote.dto.password.ChangePasswordRequest
import com.example.catering.data.remote.dto.user.AuthResponse
import com.example.catering.data.remote.dto.user.LoginRequest
import com.example.catering.data.remote.dto.user.RegisterRequest
import com.example.catering.data.remote.dto.user.RegistrationPendingResponse
import com.example.catering.data.remote.dto.user.ResendPhoneCodeRequest
import com.example.catering.data.remote.dto.user.UpdateProfileRequest
import com.example.catering.data.remote.dto.user.UserResponse
import com.example.catering.data.remote.dto.user.VerifyPhoneRequest
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getEmailStatus() = userApi.getEmailStatus()

    suspend fun registerUser(
        registerRequest: RegisterRequest
    ): RegistrationPendingResponse {
        return userApi.registerUser(registerRequest)
    }

    suspend fun verifyRegistrationPhone(
        phone: String,
        code: String
    ): AuthResponse {

        return userApi.verifyRegistrationPhone(
            VerifyPhoneRequest(
                phone = phone,
                code = code
            )
        )
    }

    suspend fun resendRegistrationPhoneCode(
        phone: String
    ) {
        val response =
            userApi.resendRegistrationPhoneCode(
                ResendPhoneCodeRequest(
                    phone = phone
                )
            )

        requireStatus(
            response = response,
            expectedStatus = 202
        )
    }

    suspend fun loginUser(
        loginRequest: LoginRequest
    ): AuthResponse {
        return userApi.loginUser(loginRequest)
    }

    suspend fun getCurrentUser(): UserResponse {
        return userApi.getCurrentUser()
    }

    suspend fun updateProfile(
        updateProfileRequest: UpdateProfileRequest
    ): UserResponse {
        return userApi.updateProfile(updateProfileRequest)
    }

    suspend fun changePassword(
        request: ChangePasswordRequest
    ) {
        val response = userApi.changePassword(request)
        requireStatus(response, 204)
    }

    // Сохранение email в профиле.
    suspend fun saveEmail(email: String) {
        val response = userApi.saveEmail(
            mapOf("email" to email)
        )
        requireStatus(response, 204)
    }

    // Отправка кода подтверждения email.
    suspend fun sendEmailVerificationCode() {
        val response = userApi.sendEmailVerificationCode()
        requireStatus(response, 202)
    }

    // Подтверждение email.
    suspend fun verifyEmail(code: String) {
        val response = userApi.verifyEmail(
            mapOf("code" to code)
        )
        requireStatus(response, 204)
    }

    // Запрос восстановления пароля.
    suspend fun forgotPassword(email: String) {
        val response = userApi.forgotPassword(
            mapOf("email" to email)
        )
        requireStatus(response, 202)
    }

    // Установка нового пароля.
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        val response = userApi.resetPassword(
            mapOf(
                "email" to email,
                "code" to code,
                "newPassword" to newPassword
            )
        )
        requireStatus(response, 204)
    }

    private fun requireStatus(
        response: Response<Unit>,
        expectedStatus: Int
    ) {
        if (response.code() != expectedStatus) {
            throw HttpException(response)
        }
    }
}
