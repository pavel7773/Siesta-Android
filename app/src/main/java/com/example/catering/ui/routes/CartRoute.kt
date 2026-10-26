package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.cart.CartItemsViewModel
import com.example.catering.presentation.cart.CartUiState
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.CartScreen

@Composable
fun CartRoute(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onGoMenu: () -> Unit,
    favoriteViewModel: FavoriteViewModel

) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val cartEntries by cartViewModel.cartEntriesState.collectAsStateWithLifecycle()
    val favoriteDishIds by favoriteViewModel
        .favoriteDishIdsState
        .collectAsStateWithLifecycle()
    val cartItemsViewModel: CartItemsViewModel = hiltViewModel()

    val cartUiState by cartItemsViewModel.cartItemsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(cartEntries) {
        cartItemsViewModel.loadCartItems(cartEntries)
    }

    when (val state = cartUiState) {

        is CartUiState.Loading -> {
            LoadingStateContent(
                message = texts.getString(R.string.ui_loading_your_cart_138)
            )
        }

        is CartUiState.Empty -> {
            CartScreen(
                cartItems = emptyList(),
                onIncrease = { cartViewModel.increaseCartItem(it) },
                onDecrease = { cartViewModel.decreaseCartItem(it) },
                onRemove = { cartViewModel.removeFromCart(it) },
                onBack = onBack,
                onCheckout = onCheckout,
                totalPrice = 0,
                onGoMenu = onGoMenu,
                favoriteDishIds = favoriteDishIds,
                onToggleFavorite = { dishId, isFavorite ->
                    favoriteViewModel.toggleFavorite(
                        dishId,
                        isFavorite
                    )
                }
            )
        }

        is CartUiState.Error -> {
            ErrorStateContent(
                title = texts.getString(R.string.ui_could_not_load_your_cart_139),
                message = texts.getString(R.string.ui_please_try_loading_again_140),
                buttonText = texts.getString(R.string.ui_retry_141),
                onButtonClick = {
                    cartItemsViewModel.loadCartItems(cartEntries)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        is CartUiState.Success -> {
            val cartItems = state.cartItems
            val totalPrice = state.totalPrice
            CartScreen(
                cartItems = cartItems,
                onIncrease = { cartViewModel.increaseCartItem(it) },
                onDecrease = { cartViewModel.decreaseCartItem(it) },
                onRemove = { cartViewModel.removeFromCart(it) },
                onBack = onBack,
                onCheckout = onCheckout,
                totalPrice = totalPrice,
                onGoMenu = onGoMenu,
                favoriteDishIds = favoriteDishIds,
                onToggleFavorite = { dishId, isFavorite ->
                    favoriteViewModel.toggleFavorite(
                        dishId,
                        isFavorite
                    )
                }
            )
        }
    }
}