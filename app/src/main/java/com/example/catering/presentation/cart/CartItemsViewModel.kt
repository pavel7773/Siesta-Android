package com.example.catering.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.cart.buildCartItems
import com.example.catering.domain.cart.calculateTotalPrice
import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.usecase.dish.GetDishesByIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface CartUiState {

    data object Loading : CartUiState
    data class Success(val cartItems: List<CartItem>, val totalPrice: Long) : CartUiState

    data object Empty : CartUiState
    data object Error : CartUiState
}
@HiltViewModel
class CartItemsViewModel @Inject constructor(
    private val getDishesByIdsUseCase: GetDishesByIdsUseCase) : ViewModel() {
    private val _cartItemsUiState =
        MutableStateFlow<CartUiState>(value = CartUiState.Empty)

    val cartItemsUiState: StateFlow<CartUiState> = _cartItemsUiState.asStateFlow()

    fun loadCartItems(cartEntries: List<CartEntry>) {
        viewModelScope.launch {
            if (cartEntries.isEmpty()) {
                _cartItemsUiState.value = CartUiState.Empty
                return@launch
            }
            try {
                _cartItemsUiState.value = CartUiState.Loading
                val dishIds = cartEntries.map { it.dishId }
                val dishes = getDishesByIdsUseCase.execute(dishIds)
                val cartItems = buildCartItems(cartEntries, menuDishes = dishes)
                val totalPrice = calculateTotalPrice(cartItems)
                if (cartItems.isEmpty()) {
                    _cartItemsUiState.value = CartUiState.Empty
                }
                else {
                    _cartItemsUiState.value = CartUiState.Success(cartItems, totalPrice)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _cartItemsUiState.value = CartUiState.Error
            }
        }
    }
}