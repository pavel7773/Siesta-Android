package com.example.catering.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderException
import com.example.catering.domain.model.PendingOrderOperation
import com.example.catering.domain.repository.PendingOrderOperationStore
import com.example.catering.domain.usecase.order.CancelOrderUseCase
import com.example.catering.domain.usecase.order.CreateOrderResult
import com.example.catering.domain.usecase.order.CreateOrderUseCase
import com.example.catering.domain.usecase.order.GetAllOrdersUseCase
import com.example.catering.domain.usecase.order.RefreshOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

sealed interface OrderSavingStatus {
    data object Idle : OrderSavingStatus
    data object Loading : OrderSavingStatus
    data class Success(val order: Order) : OrderSavingStatus
    data class Error(val message: String) : OrderSavingStatus
}

sealed interface OrderActionStatus {
    data object Idle : OrderActionStatus
    data class Success(val message: String) : OrderActionStatus
    data class Error(val message: String) : OrderActionStatus
}

private data class OrderSignature(
    val items: List<Pair<Int, Int>>,
    val name: String,
    val phone: String,
    val comment: String,
    val deliveryType: String,
    val address: String,
    val promoCode: String?
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val createOrderUseCase: CreateOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val refreshOrdersUseCase: RefreshOrdersUseCase,
    private val pendingOrderOperationStore: PendingOrderOperationStore,
    getOrderUseCase: GetAllOrdersUseCase
) : ViewModel() {
    private val _orderSavingStatus =
        MutableStateFlow<OrderSavingStatus>(value = OrderSavingStatus.Idle)
    val orderSavingStatus: StateFlow<OrderSavingStatus> = _orderSavingStatus.asStateFlow()

    private val _orderActionStatus =
        MutableStateFlow<OrderActionStatus>(value = OrderActionStatus.Idle)
    val orderActionStatus: StateFlow<OrderActionStatus> = _orderActionStatus.asStateFlow()
    val ordersState = getOrderUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshingOrders = MutableStateFlow(false)
    val isRefreshingOrders = _isRefreshingOrders.asStateFlow()

    fun clearOrderSavingError() {
        if (_orderSavingStatus.value is OrderSavingStatus.Error) {
            _orderSavingStatus.value = OrderSavingStatus.Idle
        }
    }

    private fun Order.toSignature(
        promoCode: String?
    ): OrderSignature {

        return OrderSignature(
            items = products
                .map { cartItem ->
                    cartItem.dish.id to cartItem.quantity
                }
                .sortedBy { it.first },

            name = name,
            phone = phone,
            comment = comment,
            deliveryType = deliveryType,
            address = address,
            promoCode = promoCode
        )
    }
    private fun OrderSignature.toFingerprint(): String {

        val canonicalValue =
            buildString {

                fun appendPart(value: String) {
                    append(value.length)
                    append(':')
                    append(value)
                    append('|')
                }

                items.forEach { (dishId, quantity) ->
                    appendPart(dishId.toString())
                    appendPart(quantity.toString())
                }

                appendPart(name)
                appendPart(phone)
                appendPart(comment)
                appendPart(deliveryType)
                appendPart(address)
                appendPart(promoCode.orEmpty())
            }

        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    canonicalValue.toByteArray(Charsets.UTF_8)
                )

        return bytes.joinToString(separator = "") { byte ->
            (byte.toInt() and 0xff)
                .toString(16)
                .padStart(2, '0')
        }
    }

    fun addOrder(
        order: Order,
        userId: Int,
        promoCode: String? = null
    ) {

        if (_orderSavingStatus.value is OrderSavingStatus.Loading) {
            return
        }

        _orderSavingStatus.value =
            OrderSavingStatus.Loading

        viewModelScope.launch {

            try {
                val normalizedPromoCode =
                    promoCode
                        ?.trim()
                        ?.uppercase()
                        ?.takeIf { it.isNotBlank() }

                val fingerprint =
                    order
                        .toSignature(
                            promoCode = normalizedPromoCode
                        )
                        .toFingerprint()

                val savedOperation =
                    pendingOrderOperationStore.load()

                val currentKey =
                    if (
                        savedOperation != null &&
                        savedOperation.userId == userId &&
                        savedOperation.orderFingerprint == fingerprint
                    ) {
                        savedOperation.idempotencyKey
                    } else {

                        val newKey =
                            UUID.randomUUID().toString()

                        pendingOrderOperationStore.save(
                            PendingOrderOperation(
                                userId = userId,
                                idempotencyKey = newKey,
                                orderFingerprint = fingerprint
                            )
                        )

                        newKey
                    }

                when (
                    val result =
                        createOrderUseCase.execute(
                            order = order,
                            idempotencyKey = currentKey,
                            promoCode = normalizedPromoCode
                        )
                ) {

                    is CreateOrderResult.Success -> {

                        // Сервер подтвердил создание.
                        // Pending operation больше не нужна.
                        pendingOrderOperationStore.clear()

                        _orderSavingStatus.value =
                            OrderSavingStatus.Success(
                                result.order
                            )
                    }

                    is CreateOrderResult.Error -> {

                        // Важно:
                        // pending operation НЕ удаляем.
                        // Retry должен использовать тот же key.
                        _orderSavingStatus.value =
                            OrderSavingStatus.Error(
                                result.message
                            )
                    }
                }

            } catch (e: CancellationException) {

                // Если coroutine отменена, не уничтожаем
                // pending operation: сервер мог уже получить запрос.
                throw e

            } catch (e: Exception) {

                _orderSavingStatus.value =
                    OrderSavingStatus.Error(
                        "Не удалось сохранить заказ. Проверьте историю заказов перед повторной попыткой"
                    )
            }
        }
    }

fun cancelOrder(order: Order) {
    viewModelScope.launch {
        try {
            cancelOrderUseCase.execute(order.id)

            _orderActionStatus.value =
                OrderActionStatus.Success("Заказ отменён")

        } catch (e: OrderException) {
            _orderActionStatus.value =
                OrderActionStatus.Error(
                    e.message ?: "Не удалось отменить заказ"
                )

        } catch (e: Exception) {
            _orderActionStatus.value =
                OrderActionStatus.Error("Не удалось отменить заказ")
        }
    }
}

fun clearOrderActionStatus() {
    _orderActionStatus.value = OrderActionStatus.Idle
}

fun resetSavingStatus() {
    _orderSavingStatus.value = OrderSavingStatus.Idle
}

fun refreshOrders() {
    if (_isRefreshingOrders.value) return
    _isRefreshingOrders.value = true
    viewModelScope.launch {
        try {
            refreshOrdersUseCase.execute()
        } catch (e: CancellationException) {
            throw e
        } catch (e: OrderException) {
            _orderActionStatus.value = OrderActionStatus.Error(
                "Не удалось обновить историю заказов. " +
                        (e.message ?: "Попробуйте позже")
            )
        } catch (e: Exception) {
            _orderActionStatus.value = OrderActionStatus.Error(
                "Не удалось обновить историю заказов. Попробуйте позже"
            )
        } finally {
            _isRefreshingOrders.value = false
        }
    }
}
}
