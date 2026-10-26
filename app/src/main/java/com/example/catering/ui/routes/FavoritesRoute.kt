package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.favorite.FavoriteDishesViewModel
import com.example.catering.presentation.favorite.FavoriteUiState
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.FavoriteScreen

@Composable
fun FavoritesRoute(
    favoriteViewModel: FavoriteViewModel,
    onBack: () -> Unit,
    onGoMenu: () -> Unit,
    onOpenDish: (Int) -> Unit,
    onAddToCart: (Int) -> Unit,
    badgeCartCount: Int = 0,
    onGoCart: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val favoriteDishIds by favoriteViewModel.favoriteDishIdsState.collectAsStateWithLifecycle()


    val favoriteDishViewModel: FavoriteDishesViewModel = hiltViewModel()

    LaunchedEffect(favoriteDishIds) {
        favoriteDishViewModel.loadFavoriteDishes(favoriteDishIds)
    }

    val favoriteDishesViewModel by favoriteDishViewModel.favoriteUiState.collectAsStateWithLifecycle()

    when (val state = favoriteDishesViewModel) {
        is FavoriteUiState.Loading -> {
            LoadingStateContent(
                message = texts.getString(R.string.ui_loading_favorites_144)
            )
        }

        is FavoriteUiState.Empty -> {
            FavoriteScreen(
                dishes = emptyList(),
                onBack = onBack,
                removeFavorites = { id -> favoriteViewModel.deleteFavorite(id) },
                onGoMenu = onGoMenu,
                onOpenDish = onOpenDish,
                onAddToCart = onAddToCart,
                onGoCart = onGoCart,
                badgeCartCount = badgeCartCount)
        }

        is FavoriteUiState.Error -> {
            ErrorStateContent(
                title = texts.getString(R.string.ui_could_not_load_favorites_145),
                message = texts.getString(R.string.ui_please_try_again_these_dishes_may_be_temporarily_unavailab_146),
                buttonText = texts.getString(R.string.ui_try_again_147),
                onButtonClick = {
                    favoriteDishViewModel.loadFavoriteDishes(favoriteDishIds)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        is FavoriteUiState.Success -> {
            val favoriteDishes = state.dishes
            FavoriteScreen(
                dishes = favoriteDishes,
                onBack = onBack,
                removeFavorites = { id -> favoriteViewModel.deleteFavorite(id) },
                onGoMenu = onGoMenu,
                onOpenDish = onOpenDish,
                onAddToCart = onAddToCart,
                onGoCart = onGoCart,
                badgeCartCount = badgeCartCount)
        }
    }
}