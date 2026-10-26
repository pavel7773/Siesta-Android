
package com.example.catering.data.remote.user

import com.example.catering.data.remote.dto.password.ChangePasswordRequest
import com.example.catering.data.remote.dto.user.AuthResponse
import com.example.catering.data.remote.dto.user.LoginRequest
import com.example.catering.data.remote.dto.user.RegisterRequest
import com.example.catering.data.remote.dto.user.UpdateProfileRequest
import com.example.catering.data.remote.dto.user.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @GET("auth/me/email")
    suspend fun getEmailStatus(): com.example.catering.data.remote.dto.user.EmailStatusResponse

    @POST("auth/register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): AuthResponse

    @POST("auth/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): AuthResponse

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponse

    @PATCH("auth/me")
    suspend fun updateProfile(
        @Body updateProfileRequest: UpdateProfileRequest
    ): UserResponse

    @PATCH("auth/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    // Добавление или изменение email.
    @PUT("auth/me/email")
    suspend fun saveEmail(
        @Body request: Map<String, String>
    ): Response<Unit>

    // Отправка кода подтверждения email.
    @POST("auth/me/email/send-code")
    suspend fun sendEmailVerificationCode(): Response<Unit>

    // Подтверждение email шестизначным кодом.
    @POST("auth/me/email/verify")
    suspend fun verifyEmail(
        @Body request: Map<String, String>
    ): Response<Unit>

    // Запрос восстановления забытого пароля.
    @POST("auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: Map<String, String>
    ): Response<Unit>

    // Установка нового пароля по коду.
    @POST("auth/password/reset")
    suspend fun resetPassword(
        @Body request: Map<String, String>
    ): Response<Unit>
}
