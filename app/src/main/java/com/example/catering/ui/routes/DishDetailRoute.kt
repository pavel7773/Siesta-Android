package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.dishdetail.DishDetailUiState
import com.example.catering.presentation.dishdetail.DishDetailViewModel
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.presentation.recently.RecentlyViewedViewModel
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.DishDetailScreen

@Composable
fun DishDetailRoute(
    dishId: Int,
    onBack: () -> Unit,
    cartViewModel: CartViewModel,
    favoriteViewModel: FavoriteViewModel,
    recentlyViewedViewModel: RecentlyViewedViewModel,
    onGoToCart: () -> Unit,
    badgeCartCount: Int
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val favoriteDishIds by favoriteViewModel.favoriteDishIdsState.collectAsStateWithLifecycle()
    val isFavorite = favoriteDishIds.contains(dishId)

    val dishViewModel: DishDetailViewModel = hiltViewModel()

    LaunchedEffect(dishId) {
        dishViewModel.loadDish(dishId)
    }

    val dishDetailUiState by dishViewModel.dishDetailUiState.collectAsStateWithLifecycle()

    when (val state = dishDetailUiState) {
        is DishDetailUiState.Loading -> {
            LoadingStateContent(message = texts.getString(R.string.ui_opening_dish_143))
        }

        is DishDetailUiState.Error -> {
            DishDetailScreen( onBack = onBack,
                onAddToCart = { dish ->
                    cartViewModel.addToCart(dish.id, 1)
                },
                onUndoToCart = { cartViewModel.onUndoAddToCart(it.id) },
                isFavorite = isFavorite,
                onToggleFavorite = {
                    favoriteViewModel.toggleFavorite(
                        dishId,
                        isFavorite
                    )
                },
                dish = null,
                onRetry = {dishViewModel.loadDish(dishId)},
                onGoToCart = onGoToCart,
                badgeCartCount = badgeCartCount

            )

        }

        is DishDetailUiState.Success -> {
            val dish = state.dish

            LaunchedEffect(dish.id) {
                recentlyViewedViewModel.addRecentlyViewed(dish.id)
            }

            DishDetailScreen(
                onBack = onBack,
                onAddToCart = { dish ->
                    cartViewModel.addToCart(dish.id, 1)
                },
                onUndoToCart = { cartViewModel.onUndoAddToCart(it.id) },
                isFavorite = isFavorite,
                onToggleFavorite = {
                    favoriteViewModel.toggleFavorite(
                        dishId,
                        isFavorite
                    )
                },
                dish = dish,
                onRetry = {dishViewModel.loadDish(dishId)},
                onGoToCart = onGoToCart,
                badgeCartCount = badgeCartCount
            )
        }
    }
}
