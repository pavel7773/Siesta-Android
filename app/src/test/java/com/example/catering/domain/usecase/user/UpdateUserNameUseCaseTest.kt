package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith

class UpdateUserNameUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateUserNameUseCase

    @Before
    fun setup() {
        userRepository = mockk()

        useCase = UpdateUserNameUseCase(
            userRepository = userRepository
        )
    }

    @Test
    fun `missing user returns error without repository call`() = runTest {

        val result = useCase.execute(
            user = null,
            name = "Павел"
        )

        assertEquals(
            UpdateUserNameResult.Error(
                "Пользователь не найден"
            ),
            result
        )

        coVerify(exactly = 0) {
            userRepository.updateProfile(
                any(),
                any()
            )
        }
    }

    @Test
    fun `blank name returns validation error without repository call`() =
        runTest {

            val result = useCase.execute(
                user = createUser(),
                name = "   "
            )

            assertEquals(
                UpdateUserNameResult.Error(
                    "Введите имя"
                ),
                result
            )

            coVerify(exactly = 0) {
                userRepository.updateProfile(
                    any(),
                    any()
                )
            }
        }

    @Test
    fun `successful update trims name and returns updated user`() =
        runTest {

            val updatedUser = createUser(
                name = "Новое имя"
            )

            coEvery {
                userRepository.updateProfile(
                    name = "Новое имя",
                    address = null
                )
            } returns updatedUser

            val result = useCase.execute(
                user = createUser(),
                name = "   Новое имя   "
            )

            assertEquals(
                UpdateUserNameResult.Success(
                    updatedUser
                ),
                result
            )

            coVerify(exactly = 1) {
                userRepository.updateProfile(
                    name = "Новое имя",
                    address = null
                )
            }
        }

    @Test
    fun `repository error returns update name error`() = runTest {

        coEvery {
            userRepository.updateProfile(
                name = any(),
                address = null
            )
        } throws IllegalStateException(
            "Server error"
        )

        val result = useCase.execute(
            user = createUser(),
            name = "Новое имя"
        )

        assertEquals(
            UpdateUserNameResult.Error(
                "Не удалось сохранить имя пользователя"
            ),
            result
        )
    }

    @Test
    fun `cancellation is rethrown`() = runTest {

        coEvery {
            userRepository.updateProfile(
                name = any(),
                address = null
            )
        } throws CancellationException(
            "Coroutine cancelled"
        )

        assertFailsWith<CancellationException> {

            useCase.execute(
                user = createUser(),
                name = "Новое имя"
            )
        }
    }

    private fun createUser(
        name: String = "Павел"
    ): User {
        return User(
            id = 42,
            name = name,
            phone = "79991234567",
            address = "Улица 1"
        )
    }
}