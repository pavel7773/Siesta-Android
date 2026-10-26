package com.example.catering.data.repository.user

import com.example.catering.data.local.token.TokenLocalDataSource
import com.example.catering.data.local.user.UserLocalDataSource
import com.example.catering.data.mapper.UserMapper
import com.example.catering.data.remote.dto.password.ChangePasswordRequest
import com.example.catering.data.remote.dto.user.LoginRequest
import com.example.catering.data.remote.dto.user.RegisterRequest
import com.example.catering.data.remote.dto.user.UpdateProfileRequest
import com.example.catering.data.remote.user.UserRemoteDataSource
import com.example.catering.data.remote.UserApiOperation
import com.example.catering.data.remote.userApiCall
import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userMapper: UserMapper,
    private val userLocalDataSource: UserLocalDataSource,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val tokenProvider: TokenProvider
) : UserRepository {
    override suspend fun getEmailStatus(): com.example.catering.domain.model.EmailStatus {
        val response = userRemoteDataSource.getEmailStatus()
        return com.example.catering.domain.model.EmailStatus(response.email, response.verified)
    }

    override suspend fun loginUser(phone: String, password: String): User {
        val login = LoginRequest(
            phone = phone,
            password = password
        )
        val authResponse = userApiCall(UserApiOperation.LOGIN) {
            userRemoteDataSource.loginUser(login)
        }
        tokenLocalDataSource.saveToken(token = authResponse.token)
        tokenProvider.setToken(newToken = authResponse.token)
        return userMapper.toUser(authResponse.userResponse)
    }

    override suspend fun saveUser(user: User) {
        userLocalDataSource.saveUser(user)
    }

    override suspend fun registerUser(name: String, phone: String, password: String): User {
        val register = RegisterRequest(
            name = name,
            phone = phone,
            password = password
        )
        val authResponse = userApiCall(UserApiOperation.REGISTER) {
            userRemoteDataSource.registerUser(register)
        }
        tokenLocalDataSource.saveToken(token = authResponse.token)
        tokenProvider.setToken(newToken = authResponse.token)
        return userMapper.toUser(authResponse.userResponse)
    }

    override suspend fun restoreUser(): User? {
        return userLocalDataSource.restoreUser()
    }

    override suspend fun clearToken() {
        tokenLocalDataSource.clearToken()
        tokenProvider.clearToken()
    }

    override suspend fun getToken(): String? {
        val token = tokenLocalDataSource.getToken()
        if (token != null) {
            tokenProvider.setToken(newToken = token)
        }
        return token
    }

    override suspend fun getCurrentUser(): User {
        try {
            val userResponse = userRemoteDataSource.getCurrentUser()
            return userMapper.toUser(userResponse)

        } catch (e: CancellationException) {
            throw e

        } catch (e: HttpException) {
        when (e.code()) {
            401 -> throw UserException.SessionExpired()
            in 500..599 -> throw UserException.Network()
            else -> throw UserException.Unknown()
        }
    }
    catch (e: IOException) {
            throw UserException.Network()

        } catch (e: Exception) {
            throw UserException.Unknown()
        }
    }

    override suspend fun updateProfile(
        name: String?,
        address: String?
    ): User {
        val request = UpdateProfileRequest(
            name = name,
            address = address
        )
        val userResponse = userApiCall(UserApiOperation.PROFILE) {
            userRemoteDataSource.updateProfile(request)
        }
        val user = userMapper.toUser(userResponse)
        saveUser(user)
        return user
    }

    override suspend fun clearSavedUser() {
        userLocalDataSource.clearUser()
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val request = ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword
        )

        userRemoteDataSource.changePassword(request)
    }

    override suspend fun saveEmail(email: String) {
        userRemoteDataSource.saveEmail(email)
    }

    override suspend fun sendEmailVerificationCode() {
        userRemoteDataSource.sendEmailVerificationCode()
    }

    override suspend fun verifyEmail(code: String) {
        userRemoteDataSource.verifyEmail(code)
    }

    override suspend fun forgotPassword(email: String) {
        userRemoteDataSource.forgotPassword(email)
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        userRemoteDataSource.resetPassword(
            email = email,
            code = code,
            newPassword = newPassword
        )
    }

}
