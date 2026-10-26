package com.example.catering.domain.usecase.user

import com.example.catering.domain.repository.CartRepository
import com.example.catering.domain.repository.OrderRepository
import com.example.catering.domain.repository.PendingOrderOperationStore
import com.example.catering.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface LogoutUserResult {
    data object Success : LogoutUserResult
    data class Error(
        val message: String
    ) : LogoutUserResult
}

class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val pendingOrderOperationStore: PendingOrderOperationStore
) {

    suspend fun execute(): LogoutUserResult {

        return try {

            // Сначала удаляем локальные данные пользователя.
            //
            // Token очищаем ПОСЛЕДНИМ:
            // если очистка данных сломается,
            // пользователь формально останется в своей session,
            // а не выйдет из аккаунта с чужими/старыми
            // данными, оставшимися на устройстве.

            orderRepository.clearLocalOrders()
            pendingOrderOperationStore.clear()

            cartRepository.clearCart()

            userRepository.clearSavedUser()

            userRepository.clearToken()

            LogoutUserResult.Success

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            LogoutUserResult.Error(
                "Не удалось выйти из аккаунта. Попробуйте снова"
            )
        }
    }
}
