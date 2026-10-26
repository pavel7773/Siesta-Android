package com.example.catering.data.repository.user

import com.example.catering.data.local.token.TokenLocalDataSource
import com.example.catering.data.local.user.UserLocalDataSource
import com.example.catering.data.mapper.UserMapper
import com.example.catering.data.remote.dto.user.AuthResponse
import com.example.catering.data.remote.dto.user.LoginRequest
import com.example.catering.data.remote.dto.user.RegisterRequest
import com.example.catering.data.remote.dto.user.UpdateProfileRequest
import com.example.catering.data.remote.dto.user.UserResponse
import com.example.catering.data.remote.user.UserRemoteDataSource
import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertFailsWith

class UserRepositoryImplTest {

    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userMapper: UserMapper

    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var tokenLocalDataSource: TokenLocalDataSource
    private lateinit var tokenProvider: TokenProvider

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {

        userRemoteDataSource = mockk()
        userMapper = mockk()
        userLocalDataSource = mockk()
        tokenLocalDataSource = mockk()
        tokenProvider = mockk()

        repository = UserRepositoryImpl(
            userRemoteDataSource = userRemoteDataSource,
            userMapper = userMapper,
            userLocalDataSource = userLocalDataSource,
            tokenLocalDataSource = tokenLocalDataSource,
            tokenProvider = tokenProvider
        )
    }

    @Test
    fun `get token restores saved token into memory provider`() = runTest {

        val savedToken = "saved-token-123"

        coEvery {
            tokenLocalDataSource.getToken()
        } returns savedToken

        justRun {
            tokenProvider.setToken(savedToken)
        }

        val result =
            repository.getToken()

        assertEquals(
            savedToken,
            result
        )

        coVerify(exactly = 1) {
            tokenLocalDataSource.getToken()
        }

        verify(exactly = 1) {
            tokenProvider.setToken(savedToken)
        }
    }

    @Test
    fun `get token does not update memory provider when token is missing`() =
        runTest {

            coEvery {
                tokenLocalDataSource.getToken()
            } returns null

            val result =
                repository.getToken()

            assertNull(result)

            verify(exactly = 0) {
                tokenProvider.setToken(any())
            }
        }

    @Test
    fun `clear token removes persistent and in memory tokens`() = runTest {

        coJustRun {
            tokenLocalDataSource.clearToken()
        }

        justRun {
            tokenProvider.clearToken()
        }

        repository.clearToken()

        coVerify(exactly = 1) {
            tokenLocalDataSource.clearToken()
        }

        verify(exactly = 1) {
            tokenProvider.clearToken()
        }
    }

    @Test
    fun `login saves token in persistent storage and memory and returns mapped user`() =
        runTest {

            val userResponse = UserResponse(
                id = 10,
                name = "Павел",
                phone = "79991234567",
                address = "Улица 1"
            )

            val authResponse = AuthResponse(
                userResponse = userResponse,
                token = "server-token-123"
            )

            val expectedUser = User(
                id = 10,
                name = "Павел",
                phone = "79991234567",
                address = "Улица 1"
            )

            val loginRequestSlot = slot<LoginRequest>()

            coEvery {
                userRemoteDataSource.loginUser(
                    capture(loginRequestSlot)
                )
            } returns authResponse

            coJustRun {
                tokenLocalDataSource.saveToken(
                    "server-token-123"
                )
            }

            justRun {
                tokenProvider.setToken(
                    "server-token-123"
                )
            }

            every {
                userMapper.toUser(userResponse)
            } returns expectedUser

            val result = repository.loginUser(
                phone = "79991234567",
                password = "password123"
            )

            assertEquals(
                expectedUser,
                result
            )

            assertEquals(
                "79991234567",
                loginRequestSlot.captured.phone
            )

            assertEquals(
                "password123",
                loginRequestSlot.captured.password
            )

            coVerify(exactly = 1) {
                tokenLocalDataSource.saveToken(
                    "server-token-123"
                )
            }

            verify(exactly = 1) {
                tokenProvider.setToken(
                    "server-token-123"
                )
            }

            verify(exactly = 1) {
                userMapper.toUser(userResponse)
            }
        }

    @Test
    fun `register saves token in persistent storage and memory and returns mapped user`() =
        runTest {

            val userResponse = UserResponse(
                id = 20,
                name = "Иван",
                phone = "79990000000",
                address = ""
            )

            val authResponse = AuthResponse(
                userResponse = userResponse,
                token = "register-token-456"
            )

            val expectedUser = User(
                id = 20,
                name = "Иван",
                phone = "79990000000",
                address = ""
            )

            val requestSlot = slot<RegisterRequest>()

            coEvery {
                userRemoteDataSource.registerUser(
                    capture(requestSlot)
                )
            } returns authResponse

            coJustRun {
                tokenLocalDataSource.saveToken(
                    "register-token-456"
                )
            }

            justRun {
                tokenProvider.setToken(
                    "register-token-456"
                )
            }

            every {
                userMapper.toUser(userResponse)
            } returns expectedUser

            val result = repository.registerUser(
                name = "Иван",
                phone = "79990000000",
                password = "password123"
            )

            assertEquals(
                expectedUser,
                result
            )

            assertEquals(
                "Иван",
                requestSlot.captured.name
            )

            assertEquals(
                "79990000000",
                requestSlot.captured.phone
            )

            assertEquals(
                "password123",
                requestSlot.captured.password
            )

            coVerify(exactly = 1) {
                tokenLocalDataSource.saveToken(
                    "register-token-456"
                )
            }

            verify(exactly = 1) {
                tokenProvider.setToken(
                    "register-token-456"
                )
            }
        }
    @Test
    fun `get current user returns mapped server user`() = runTest {

        val response = UserResponse(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        val expectedUser = User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        coEvery {
            userRemoteDataSource.getCurrentUser()
        } returns response

        every {
            userMapper.toUser(response)
        } returns expectedUser

        val result =
            repository.getCurrentUser()

        assertEquals(
            expectedUser,
            result
        )

        coVerify(exactly = 1) {
            userRemoteDataSource.getCurrentUser()
        }

        verify(exactly = 1) {
            userMapper.toUser(response)
        }
    }
    @Test
    fun `get current user unauthorized throws session expired`() = runTest {

        val response401 =
            Response.error<Any>(
                401,
                "".toResponseBody()
            )

        coEvery {
            userRemoteDataSource.getCurrentUser()
        } throws HttpException(response401)

        assertFailsWith<UserException.SessionExpired> {
            repository.getCurrentUser()
        }
    }
    @Test
    fun `get current user network error throws user network exception`() =
        runTest {

            coEvery {
                userRemoteDataSource.getCurrentUser()
            } throws IOException("No internet")

            assertFailsWith<UserException.Network> {
                repository.getCurrentUser()
            }
        }
    @Test
    fun `get current user unexpected error throws unknown exception`() =
        runTest {

            coEvery {
                userRemoteDataSource.getCurrentUser()
            } throws IllegalStateException("Something went wrong")

            assertFailsWith<UserException.Unknown> {
                repository.getCurrentUser()
            }
        }
    @Test
    fun `get current user cancellation is rethrown`() = runTest {

        coEvery {
            userRemoteDataSource.getCurrentUser()
        } throws CancellationException("Coroutine cancelled")

        assertFailsWith<CancellationException> {
            repository.getCurrentUser()
        }
    }

    @Test
    fun `restore user returns user from local data source`() = runTest {

        val savedUser = User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        coEvery {
            userLocalDataSource.restoreUser()
        } returns savedUser

        val result = repository.restoreUser()

        assertEquals(
            savedUser,
            result
        )

        coVerify(exactly = 1) {
            userLocalDataSource.restoreUser()
        }
    }
    @Test
    fun `save user delegates user to local data source`() = runTest {

        val user = User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )

        coJustRun {
            userLocalDataSource.saveUser(user)
        }

        repository.saveUser(user)

        coVerify(exactly = 1) {
            userLocalDataSource.saveUser(user)
        }
    }
    @Test
    fun `restore user returns null when local user is missing`() = runTest {

        coEvery {
            userLocalDataSource.restoreUser()
        } returns null

        val result = repository.restoreUser()

        assertNull(result)

        coVerify(exactly = 1) {
            userLocalDataSource.restoreUser()
        }
    }
    @Test
    fun `update profile sends request maps response and saves updated user`() =
        runTest {

            val requestSlot =
                slot<UpdateProfileRequest>()

            val userResponse = UserResponse(
                id = 42,
                name = "Новое имя",
                phone = "79991234567",
                address = "Новый адрес"
            )

            val expectedUser = User(
                id = 42,
                name = "Новое имя",
                phone = "79991234567",
                address = "Новый адрес"
            )

            coEvery {
                userRemoteDataSource.updateProfile(
                    capture(requestSlot)
                )
            } returns userResponse

            every {
                userMapper.toUser(userResponse)
            } returns expectedUser

            coJustRun {
                userLocalDataSource.saveUser(expectedUser)
            }

            val result = repository.updateProfile(
                name = "Новое имя",
                address = "Новый адрес"
            )

            assertEquals(
                "Новое имя",
                requestSlot.captured.name
            )

            assertEquals(
                "Новый адрес",
                requestSlot.captured.address
            )

            assertEquals(
                expectedUser,
                result
            )

            coVerify(exactly = 1) {
                userRemoteDataSource.updateProfile(any())
            }

            verify(exactly = 1) {
                userMapper.toUser(userResponse)
            }

            coVerify(exactly = 1) {
                userLocalDataSource.saveUser(expectedUser)
            }
        }
    @Test
    fun `login remote error does not save token`() = runTest {

        coEvery {
            userRemoteDataSource.loginUser(any())
        } throws IOException("No internet")

        assertFailsWith<UserException.Network> {
            repository.loginUser(
                phone = "79991234567",
                password = "password123"
            )
        }

        coVerify(exactly = 0) {
            tokenLocalDataSource.saveToken(any())
        }

        verify(exactly = 0) {
            tokenProvider.setToken(any())
        }
    }
}