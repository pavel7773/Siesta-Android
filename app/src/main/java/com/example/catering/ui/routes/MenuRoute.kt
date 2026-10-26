package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.presentation.menu.MenuViewModel
import com.example.catering.ui.screens.MenuScreen

@Composable
fun MenuRoute(
    onOpenDish: (Int) -> Unit,
    onBack: () -> Unit,
    onGoCart: () -> Unit,
    onGoSupport: () -> Unit,
    favoriteViewModel: FavoriteViewModel,
    menuViewModel: MenuViewModel,
    badgeCartCount: Int = 0,
    onAddToCart: (Int) -> Unit
) {
    val openFiltersRequested by menuViewModel.openFiltersRequested.collectAsStateWithLifecycle()
    val favoriteDishIds by favoriteViewModel.favoriteDishIdsState.collectAsStateWithLifecycle()
    val searchQuery by menuViewModel.searchQuery.collectAsStateWithLifecycle()
    val displayMenuUiState by menuViewModel.displayMenuUiState.collectAsStateWithLifecycle()
    val selectedCategory by menuViewModel.selectedCategory.collectAsStateWithLifecycle()
    val availableCategories by menuViewModel.availableCategories.collectAsStateWithLifecycle()
    val selectedSort by menuViewModel.selectedSort.collectAsStateWithLifecycle()
    val priceRange by menuViewModel.selectedPriceRange.collectAsStateWithLifecycle()

    MenuScreen(
        openFiltersRequested = openFiltersRequested,
        onFiltersRequestConsumed = menuViewModel::consumeFiltersRequest,
        onOpenDish = onOpenDish,
        onBack = onBack,
        onGoCart = onGoCart,
        onGoSupport = onGoSupport,
        onToggleFavorite = { id, isFavorite ->
            favoriteViewModel.toggleFavorite(
                id,
                isFavorite
            )
        },
        menuUiState = displayMenuUiState,
        favoriteDishIds = favoriteDishIds,
        searchQuery = searchQuery,
        onSearchQueryChange = { menuViewModel.updateSearchQuery(it) },
        selectedCategory = selectedCategory,
        onCategoryChange = { menuViewModel.updateSelectedCategory(it) },
        categories = availableCategories,
        onSortChange = { sort ->
            menuViewModel.updateSelectedSort(sort)
        },
        selectedSort = selectedSort,
        onPriceRangeChange = { menuViewModel.updateSelectedPriceRange(it) },
        onClearPriceRange = { menuViewModel.clearSelectedPriceRange() },
        selectedPriceRange = priceRange,
        onRetry = { menuViewModel.refreshMenu() },
        onRefresh = { menuViewModel.refreshMenu() },
        badgeCartCount = badgeCartCount,
        onAddToCart = onAddToCart
    )
}