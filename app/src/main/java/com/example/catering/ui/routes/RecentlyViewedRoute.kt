package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.presentation.recently.RecentlyUiState
import com.example.catering.presentation.recently.RecentlyViewedDishesViewModel
import com.example.catering.presentation.recently.RecentlyViewedViewModel
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.RecentlyViewed

@Composable
fun RecentlyViewedRoute(
    recentlyViewedViewModel: RecentlyViewedViewModel,
    onBack: () -> Unit,
    favoriteViewModel: FavoriteViewModel,
    onGoMenu: () -> Unit,
    onOpenDish: (Int) -> Unit,
    onGoCart: () -> Unit,
    badgeCartCount: Int,
    onAddToCart: (Int) -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val favoriteIds by favoriteViewModel.favoriteDishIdsState.collectAsStateWithLifecycle()
    val recentlyViewedIds by recentlyViewedViewModel.recentlyViewedDishIdsState.collectAsStateWithLifecycle()

    val recentlyDishesViewModel: RecentlyViewedDishesViewModel = hiltViewModel()

    val recentlyUiState by recentlyDishesViewModel.recentlyViewedUiState.collectAsStateWithLifecycle()

    LaunchedEffect(recentlyViewedIds) {
        recentlyDishesViewModel.loadRecentlyViewed( recentlyViewedIds)
    }

    when (val state = recentlyUiState) {
        is RecentlyUiState.Loading -> {
            LoadingStateContent(
                message = texts.getString(R.string.ui_loading_recently_viewed_dishes_153)
            )
        }

        is RecentlyUiState.Empty -> {
            RecentlyViewed(
                onBack = onBack,
                onGoMenu = onGoMenu,
                onGoCart = onGoCart,
                badgeCartCount = badgeCartCount,
                onOpenDish = onOpenDish,
                onAddToCart = onAddToCart,
                favoriteIds = favoriteIds,
                onToggleFavorite = favoriteViewModel::toggleFavorite,
                clear = { recentlyViewedViewModel.clearRecentlyViewed() },
                dishes = emptyList()
            )
        }

        is RecentlyUiState.Error -> {
            ErrorStateContent(
                title = texts.getString(R.string.ui_could_not_load_recently_viewed_dishes_154),
                message = texts.getString(R.string.ui_please_try_again_155),
                buttonText = texts.getString(R.string.ui_try_again_147),
                onButtonClick = {
                    recentlyDishesViewModel.loadRecentlyViewed(recentlyViewedIds)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        is RecentlyUiState.Success -> {
            val dishes = state.dishes

            RecentlyViewed(
                onBack = onBack,
                onGoMenu = onGoMenu,
                onGoCart = onGoCart,
                badgeCartCount = badgeCartCount,
                onOpenDish = onOpenDish,
                onAddToCart = onAddToCart,
                favoriteIds = favoriteIds,
                onToggleFavorite = favoriteViewModel::toggleFavorite,
                clear = { recentlyViewedViewModel.clearRecentlyViewed() },
                dishes = dishes
            )
        }
    }
}