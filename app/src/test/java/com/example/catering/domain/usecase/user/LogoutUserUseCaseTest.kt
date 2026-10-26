package com.example.catering.domain.usecase.user

import com.example.catering.domain.repository.CartRepository
import com.example.catering.domain.repository.OrderRepository
import com.example.catering.domain.repository.PendingOrderOperationStore
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

class LogoutUserUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var cartRepository: CartRepository

    private lateinit var pendingOrderOperationStore: PendingOrderOperationStore
    private lateinit var useCase: LogoutUserUseCase

    @Before
    fun setup() {

        userRepository = mockk()
        orderRepository = mockk()
        cartRepository = mockk()

        pendingOrderOperationStore = mockk()

        coJustRun {
            pendingOrderOperationStore.clear()
        }

        useCase = LogoutUserUseCase(
            userRepository = userRepository,
            orderRepository = orderRepository,
            cartRepository = cartRepository,
            pendingOrderOperationStore = pendingOrderOperationStore
        )
    }

    @Test
    fun `successful logout clears private data and token in correct order`() =
        runTest {

            coJustRun {
                orderRepository.clearLocalOrders()
            }

            coJustRun {
                cartRepository.clearCart()
            }

            coJustRun {
                userRepository.clearSavedUser()
            }

            coJustRun {
                userRepository.clearToken()
            }

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Success,
                result
            )

            // Важен не только сам факт очистки,
            // но и порядок.
            //
            // Token удаляется последним:
            // сначала очищаем приватные локальные данные,
            // затем завершаем session.
            coVerifyOrder {

                orderRepository.clearLocalOrders()

                pendingOrderOperationStore.clear()

                cartRepository.clearCart()

                userRepository.clearSavedUser()

                userRepository.clearToken()
            }
        }

    @Test
    fun `order cleanup error returns error and does not clear token`() =
        runTest {

            coEvery {
                orderRepository.clearLocalOrders()
            } throws IllegalStateException(
                "Database error"
            )

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Error(
                    "Не удалось выйти из аккаунта. Попробуйте снова"
                ),
                result
            )

            coVerify(exactly = 0) {
                pendingOrderOperationStore.clear()
            }

            coVerify(exactly = 0) {
                cartRepository.clearCart()
            }

            coVerify(exactly = 0) {
                userRepository.clearSavedUser()
            }

            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }

    @Test
    fun `cart cleanup error does not clear saved user or token`() =
        runTest {

            coJustRun {
                orderRepository.clearLocalOrders()
            }

            coEvery {
                cartRepository.clearCart()
            } throws IllegalStateException(
                "Cart cleanup failed"
            )

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Error(
                    "Не удалось выйти из аккаунта. Попробуйте снова"
                ),
                result
            )
            coVerify(exactly = 1) {
                pendingOrderOperationStore.clear()
            }

            coVerify(exactly = 1) {
                orderRepository.clearLocalOrders()
            }

            coVerify(exactly = 0) {
                userRepository.clearSavedUser()
            }

            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }

    @Test
    fun `saved user cleanup error does not clear token`() =
        runTest {

            coJustRun {
                orderRepository.clearLocalOrders()
            }

            coJustRun {
                cartRepository.clearCart()
            }

            coEvery {
                userRepository.clearSavedUser()
            } throws IllegalStateException(
                "User cleanup failed"
            )

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Error(
                    "Не удалось выйти из аккаунта. Попробуйте снова"
                ),
                result
            )

            // Самое важное:
            // если saved User удалить не удалось,
            // token не удаляем.
            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }

    @Test
    fun `token cleanup error returns logout error`() =
        runTest {

            coJustRun {
                orderRepository.clearLocalOrders()
            }

            coJustRun {
                cartRepository.clearCart()
            }

            coJustRun {
                userRepository.clearSavedUser()
            }

            coEvery {
                userRepository.clearToken()
            } throws IllegalStateException(
                "Token cleanup failed"
            )

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Error(
                    "Не удалось выйти из аккаунта. Попробуйте снова"
                ),
                result
            )

            coVerify(exactly = 1) {
                orderRepository.clearLocalOrders()
            }

            coVerify(exactly = 1) {
                cartRepository.clearCart()
            }

            coVerify(exactly = 1) {
                userRepository.clearSavedUser()
            }

            coVerify(exactly = 1) {
                userRepository.clearToken()
            }
        }

    @Test
    fun `cancellation is rethrown`() =
        runTest {

            coEvery {
                orderRepository.clearLocalOrders()
            } throws CancellationException(
                "Coroutine cancelled"
            )

            assertFailsWith<CancellationException> {
                useCase.execute()
            }

            coVerify(exactly = 0) {
                cartRepository.clearCart()
            }

            coVerify(exactly = 0) {
                userRepository.clearSavedUser()
            }

            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }
    @Test
    fun `pending order cleanup error stops logout and does not clear token`() =
        runTest {

            coJustRun {
                orderRepository.clearLocalOrders()
            }

            coEvery {
                pendingOrderOperationStore.clear()
            } throws IllegalStateException(
                "Pending order cleanup failed"
            )

            val result =
                useCase.execute()

            assertEquals(
                LogoutUserResult.Error(
                    "Не удалось выйти из аккаунта. Попробуйте снова"
                ),
                result
            )

            coVerify(exactly = 1) {
                orderRepository.clearLocalOrders()
            }

            coVerify(exactly = 1) {
                pendingOrderOperationStore.clear()
            }

            coVerify(exactly = 0) {
                cartRepository.clearCart()
            }

            coVerify(exactly = 0) {
                userRepository.clearSavedUser()
            }

            coVerify(exactly = 0) {
                userRepository.clearToken()
            }
        }
}