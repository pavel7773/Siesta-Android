package com.example.catering.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Order
import com.example.catering.domain.usecase.cart.AddToCartUseCase
import com.example.catering.domain.usecase.cart.ClearCartUseCase
import com.example.catering.domain.usecase.cart.DecreaseCartItemUseCase
import com.example.catering.domain.usecase.cart.GetCartUseCase
import com.example.catering.domain.usecase.cart.IncreaseCartItemUseCase
import com.example.catering.domain.usecase.cart.RemoveFromCartUseCase
import com.example.catering.domain.usecase.cart.RepeatOrderResult
import com.example.catering.domain.usecase.cart.RepeatOrderUseCase
import com.example.catering.domain.usecase.cart.UndoAddToCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val undoAddToCartUseCase: UndoAddToCartUseCase,
    private val increaseCartItemUseCase: IncreaseCartItemUseCase,
    private val decreaseCartItemUseCase: DecreaseCartItemUseCase,
    private val repeatOrderUseCase: RepeatOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase
) : ViewModel() {
    private val _navigateToCartEvent = Channel<Unit>()
    val navigateToCartEvent = _navigateToCartEvent.receiveAsFlow()
    private val _cartMessageEvent = Channel<String>()
    val cartMessageEvent = _cartMessageEvent.receiveAsFlow()
    val cartEntriesState =
        getCartUseCase.execute().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    val totalItemsCount =
        cartEntriesState.map { items ->
            items.sumOf { it.quantity }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun repeatOrder(
        order: Order
    ) {
        viewModelScope.launch {

            try {

                when (repeatOrderUseCase.execute(order)) {

                    RepeatOrderResult.Success -> {
                        _cartMessageEvent.send(
                            "Заказ добавлен в корзину"
                        )
                        _navigateToCartEvent.send(Unit)
                    }

                    RepeatOrderResult.PartialSuccess -> {
                        _cartMessageEvent.send(
                            "Некоторые блюда из заказа больше недоступны"
                        )
                        _navigateToCartEvent.send(Unit)
                    }

                    RepeatOrderResult.NoAvailableDishes -> {
                        _cartMessageEvent.send(
                            "Блюда из этого заказа больше недоступны"
                        )
                    }

                    RepeatOrderResult.EmptyOrder -> {
                        _cartMessageEvent.send(
                            "Заказ пустой, повторить нечего"
                        )
                    }
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _cartMessageEvent.send(
                    "Не удалось повторить заказ"
                )
            }
        }
    }

    fun addToCart(
        dishId: Int,
        count: Int
    ) {
        viewModelScope.launch {

            addToCartUseCase.execute(
                dishId = dishId,
                count = count
            )
        }
    }

    fun onUndoAddToCart(
        dishId: Int
    ) {
        viewModelScope.launch {

            undoAddToCartUseCase.execute(
                dishId = dishId
            )
        }
    }

    fun increaseCartItem(
        cartItem: CartItem
    ) {
        viewModelScope.launch {

            increaseCartItemUseCase.execute(
                dishId = cartItem.dish.id
            )
        }
    }

    fun decreaseCartItem(
        cartItem: CartItem
    ) {
        viewModelScope.launch {

            decreaseCartItemUseCase.execute(
                dishId = cartItem.dish.id
            )
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            removeFromCartUseCase.execute(cartItem.dish.id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase.execute()
        }
    }
}


