package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith

class RestoreSessionUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: RestoreSessionUseCase

    @Before
    fun setup() {

        userRepository = mockk()

        useCase = RestoreSessionUseCase(
            userRepository = userRepository
        )
    }

    @Test
    fun `missing token returns success with no user`() = runTest {

        coEvery {
            userRepository.getToken()
        } returns null

        val result =
            useCase.execute()

        assertEquals(
            RestoreSessionResult.Success(
                currentUser = null
            ),
            result
        )

        coVerify(exactly = 1) {
            userRepository.getToken()
        }

        // Если token отсутствует,
        // дальше проверять серверную session бессмысленно.
        coVerify(exactly = 0) {
            userRepository.getCurrentUser()
        }

        coVerify(exactly = 0) {
            userRepository.restoreUser()
        }
    }

    @Test
    fun `valid token restores current user from server and saves user locally`() =
        runTest {

            val user = createUser()

            coEvery {
                userRepository.getToken()
            } returns "token-123"

            coEvery {
                userRepository.getCurrentUser()
            } returns user

            coJustRun {
                userRepository.saveUser(user)
            }

            val result =
                useCase.execute()

            assertEquals(
                RestoreSessionResult.Success(
                    currentUser = user
                ),
                result
            )

            coVerify(exactly = 1) {
                userRepository.saveUser(user)
            }

            coVerify(exactly = 0) {
                userRepository.restoreUser()
            }

            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }

    @Test
    fun `session expired clears token and returns no user`() = runTest {

        coEvery {
            userRepository.getToken()
        } returns "expired-token"

        coEvery {
            userRepository.getCurrentUser()
        } throws UserException.SessionExpired()

        coJustRun {
            userRepository.clearToken()
        }

        val result =
            useCase.execute()

        assertEquals(
            RestoreSessionResult.Success(
                currentUser = null
            ),
            result
        )

        coVerify(exactly = 1) {
            userRepository.clearToken()
        }

        coVerify(exactly = 0) {
            userRepository.restoreUser()
        }

        coVerify(exactly = 0) {
            userRepository.saveUser(any())
        }
    }

    @Test
    fun `network error restores saved user for offline session`() = runTest {

        val savedUser = createUser()

        coEvery {
            userRepository.getToken()
        } returns "token-123"

        coEvery {
            userRepository.getCurrentUser()
        } throws UserException.Network()

        coEvery {
            userRepository.restoreUser()
        } returns savedUser

        val result =
            useCase.execute()

        assertEquals(
            RestoreSessionResult.Success(
                currentUser = savedUser
            ),
            result
        )

        coVerify(exactly = 1) {
            userRepository.restoreUser()
        }

        coVerify(exactly = 0) {
            userRepository.clearToken()
        }

        coVerify(exactly = 0) {
            userRepository.saveUser(any())
        }
    }

    @Test
    fun `network error with no saved user returns success with no user`() =
        runTest {

            coEvery {
                userRepository.getToken()
            } returns "token-123"

            coEvery {
                userRepository.getCurrentUser()
            } throws UserException.Network()

            coEvery {
                userRepository.restoreUser()
            } returns null

            val result =
                useCase.execute()

            assertEquals(
                RestoreSessionResult.Success(
                    currentUser = null
                ),
                result
            )

            coVerify(exactly = 1) {
                userRepository.restoreUser()
            }
        }

    @Test
    fun `cancellation is rethrown`() = runTest {

        coEvery {
            userRepository.getToken()
        } returns "token-123"

        coEvery {
            userRepository.getCurrentUser()
        } throws CancellationException(
            "Coroutine cancelled"
        )

        assertFailsWith<CancellationException> {
            useCase.execute()
        }

        // После отмены coroutine никакую дополнительную
        // работу по восстановлению session выполнять нельзя.
        coVerify(exactly = 0) {
            userRepository.restoreUser()
        }

        coVerify(exactly = 0) {
            userRepository.clearToken()
        }
    }

    @Test
    fun `unexpected error returns restore session error`() = runTest {

        coEvery {
            userRepository.getToken()
        } throws IllegalStateException(
            "Storage error"
        )

        val result =
            useCase.execute()

        assertEquals(
            RestoreSessionResult.Error(
                message = "Не удалось восстановить пользователя. Попробуйте снова"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.getCurrentUser()
        }
    }

    @Test
    fun `successful restore executes session steps in correct order`() =
        runTest {

            val user = createUser()

            coEvery {
                userRepository.getToken()
            } returns "token-123"

            coEvery {
                userRepository.getCurrentUser()
            } returns user

            coJustRun {
                userRepository.saveUser(user)
            }

            useCase.execute()

            coVerifyOrder {

                userRepository.getToken()

                userRepository.getCurrentUser()

                userRepository.saveUser(user)
            }
        }

    private fun createUser(): User {

        return User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )
    }
}