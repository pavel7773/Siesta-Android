package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.RegistrationPending
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

class RegisterUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: RegisterUserUseCase

    @Before
    fun setup() {
        userRepository = mockk()

        useCase = RegisterUserUseCase(
            userRepository = userRepository
        )
    }

    @Test
    fun `blank name returns validation error`() = runTest {

        val result = useCase.execute(
            name = "   ",
            phone = "79991234567",
            password = "123456",
            passwordRepeat = "123456"
        )

        assertEquals(
            RegisterUserResult.Error("Введите имя"),
            result
        )

        coVerify(exactly = 0) {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `invalid phone returns validation error`() = runTest {

        val result = useCase.execute(
            name = "Павел",
            phone = "123",
            password = "123456",
            passwordRepeat = "123456"
        )

        assertEquals(
            RegisterUserResult.Error(
                "Введите корректный номер телефона"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `short password returns validation error`() = runTest {

        val result = useCase.execute(
            name = "Павел",
            phone = "79991234567",
            password = "12345",
            passwordRepeat = "12345"
        )

        assertEquals(
            RegisterUserResult.Error(
                "Введите пароль больше 6 символов"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `different passwords return validation error`() = runTest {

        val result = useCase.execute(
            name = "Павел",
            phone = "79991234567",
            password = "123456",
            passwordRepeat = "654321"
        )

        assertEquals(
            RegisterUserResult.Error(
                "Пароли не совпадают"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `successful registration trims name and phone and returns verification required`() =
        runTest {

            val pending =
                RegistrationPending(
                    phone = "79991234567",
                    codeSent = true
                )

            coEvery {
                userRepository.registerUser(
                    name = "Павел",
                    phone = "79991234567",
                    password = "123456"
                )
            } returns pending

            val result =
                useCase.execute(
                    name = "  Павел  ",
                    phone = "  79991234567  ",
                    password = "123456",
                    passwordRepeat = "123456"
                )

            assertEquals(
                RegisterUserResult.VerificationRequired(
                    phone = "79991234567",
                    codeSent = true
                ),
                result
            )

            coVerify(exactly = 1) {
                userRepository.registerUser(
                    name = "Павел",
                    phone = "79991234567",
                    password = "123456"
                )
            }

            coVerify(exactly = 0) {
                userRepository.saveUser(any())
            }
        }

    @Test
    fun `repository error returns registration error`() = runTest {

        coEvery {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        } throws IllegalStateException(
            "Registration failed"
        )

        val result = useCase.execute(
            name = "Павел",
            phone = "79991234567",
            password = "123456",
            passwordRepeat = "123456"
        )

        assertEquals(
            RegisterUserResult.Error(
                "Не удалось зарегистрировать пользователя"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.saveUser(any())
        }
    }

    @Test
    fun `cancellation is rethrown`() = runTest {

        coEvery {
            userRepository.registerUser(
                any(),
                any(),
                any()
            )
        } throws CancellationException(
            "Coroutine cancelled"
        )

        assertFailsWith<CancellationException> {
            useCase.execute(
                name = "Павел",
                phone = "79991234567",
                password = "123456",
                passwordRepeat = "123456"
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
            address = ""
        )
    }
}