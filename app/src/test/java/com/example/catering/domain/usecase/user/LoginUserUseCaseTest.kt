package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith

class LoginUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: LoginUserUseCase

    @Before
    fun setup() {
        userRepository = mockk()

        useCase = LoginUserUseCase(
            userRepository = userRepository
        )
    }

    @Test
    fun `blank phone returns validation error without repository call`() =
        runTest {

            val result = useCase.execute(
                phone = "   ",
                password = "123456"
            )

            assertEquals(
                LoginUserResult.Error(
                    "Введите корректный номер телефона"
                ),
                result
            )

            coVerify(exactly = 0) {
                userRepository.loginUser(any(), any())
            }
        }

    @Test
    fun `short phone returns validation error without repository call`() =
        runTest {

            val result = useCase.execute(
                phone = "123456789",
                password = "123456"
            )

            assertEquals(
                LoginUserResult.Error(
                    "Введите корректный номер телефона"
                ),
                result
            )

            coVerify(exactly = 0) {
                userRepository.loginUser(any(), any())
            }
        }

    @Test
    fun `password shorter than six characters returns validation error`() =
        runTest {

            val result = useCase.execute(
                phone = "79991234567",
                password = "12345"
            )

            assertEquals(
                LoginUserResult.Error(
                    "Введите пароль больше 6 символов"
                ),
                result
            )

            coVerify(exactly = 0) {
                userRepository.loginUser(any(), any())
            }
        }

    @Test
    fun `successful login trims phone saves user and returns success`() =
        runTest {

            val user = createUser()

            coEvery {
                userRepository.loginUser(
                    phone = "79991234567",
                    password = "123456"
                )
            } returns user

            coJustRun {
                userRepository.saveUser(user)
            }

            val result = useCase.execute(
                phone = "  79991234567  ",
                password = "123456"
            )

            assertEquals(
                LoginUserResult.Success(user),
                result
            )

            coVerify(exactly = 1) {
                userRepository.loginUser(
                    phone = "79991234567",
                    password = "123456"
                )
            }

            coVerify(exactly = 1) {
                userRepository.saveUser(user)
            }
        }

    @Test
    fun `repository error returns login error`() = runTest {

        coEvery {
            userRepository.loginUser(
                any(),
                any()
            )
        } throws IllegalStateException("Login failed")

        val result = useCase.execute(
            phone = "79991234567",
            password = "123456"
        )

        assertEquals(
            LoginUserResult.Error("Не удалось выполнить вход"),
            result
        )

        coVerify(exactly = 0) {
            userRepository.saveUser(any())
        }
    }

    @Test
    fun `cancellation is rethrown`() = runTest {

        coEvery {
            userRepository.loginUser(
                any(),
                any()
            )
        } throws CancellationException(
            "Coroutine cancelled"
        )

        assertFailsWith<CancellationException> {
            useCase.execute(
                phone = "79991234567",
                password = "123456"
            )
        }

        coVerify(exactly = 0) {
            userRepository.saveUser(any())
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