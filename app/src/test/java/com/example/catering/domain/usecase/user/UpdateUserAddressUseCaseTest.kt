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

class UpdateUserAddressUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateUserAddressUseCase

    @Before
    fun setup() {
        userRepository = mockk()

        useCase = UpdateUserAddressUseCase(
            userRepository = userRepository
        )
    }

    @Test
    fun `missing user returns error without repository call`() = runTest {

        val result = useCase.execute(
            user = null,
            address = "Новый адрес"
        )

        assertEquals(
            UpdateUserAddressResult.Error(
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
    fun `successful update trims address and returns updated user`() =
        runTest {

            val updatedUser = createUser(
                address = "Новый адрес"
            )

            coEvery {
                userRepository.updateProfile(
                    name = null,
                    address = "Новый адрес"
                )
            } returns updatedUser

            val result = useCase.execute(
                user = createUser(),
                address = "   Новый адрес   "
            )

            assertEquals(
                UpdateUserAddressResult.Success(
                    updatedUser
                ),
                result
            )

            coVerify(exactly = 1) {
                userRepository.updateProfile(
                    name = null,
                    address = "Новый адрес"
                )
            }
        }

    @Test
    fun `repository error returns update address error`() = runTest {

        coEvery {
            userRepository.updateProfile(
                name = null,
                address = any()
            )
        } throws IllegalStateException(
            "Server error"
        )

        val result = useCase.execute(
            user = createUser(),
            address = "Новый адрес"
        )

        assertEquals(
            UpdateUserAddressResult.Error(
                "Не удалось сохранить адрес"
            ),
            result
        )
    }

    @Test
    fun `cancellation is rethrown`() = runTest {

        coEvery {
            userRepository.updateProfile(
                name = null,
                address = any()
            )
        } throws CancellationException(
            "Coroutine cancelled"
        )

        assertFailsWith<CancellationException> {

            useCase.execute(
                user = createUser(),
                address = "Новый адрес"
            )
        }
    }

    private fun createUser(
        address: String = "Улица 1"
    ): User {
        return User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = address
        )
    }
}