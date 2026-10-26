package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.domain.model.Dish
import com.example.catering.presentation.menu.MenuViewModel
import com.example.catering.presentation.menu.MenuUiState
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.recently.RecentlyUiState
import com.example.catering.presentation.recently.RecentlyViewedDishesViewModel
import com.example.catering.presentation.recently.RecentlyViewedViewModel
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.HomeScreen

@Composable
fun HomeRoute(
    cartViewModel: CartViewModel,
    userViewModel: UserViewModel,
    menuViewModel: MenuViewModel,
    onGoMenu: () -> Unit,
    onGoToCart: () -> Unit,
    onExitApp: () -> Unit,
    onGoViewedDish: () -> Unit,
    onOpenRecentlyViewedDish: ((Int) -> Unit)? = null,
    onAddRecentlyViewedToCart: ((Dish) -> Unit)? = null
) {
    val query by menuViewModel.searchQuery.collectAsStateWithLifecycle()
    val menuState by menuViewModel.menuDishes.collectAsStateWithLifecycle()
    val categoryDishes = (menuState as? MenuUiState.Success)?.dishes.orEmpty()
        .filter { it.category.isNotBlank() }.distinctBy { it.category }
        .sortedBy { dish ->
            val order = listOf("Паста", "Пицца", "Салаты", "Морепродукты", "Завтраки")
            order.indexOf(dish.category).takeIf { it >= 0 } ?: order.size
        }
    val totalItemsCount by cartViewModel.totalItemsCount
        .collectAsStateWithLifecycle()

    val recentlyViewedViewModel: RecentlyViewedViewModel = hiltViewModel()
    val recentlyDishesViewModel: RecentlyViewedDishesViewModel = hiltViewModel()

    val recentlyViewedIds by recentlyViewedViewModel
        .recentlyViewedDishIdsState
        .collectAsStateWithLifecycle()

    val recentlyUiState by recentlyDishesViewModel
        .recentlyViewedUiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(recentlyViewedIds) {
        recentlyDishesViewModel.loadRecentlyViewed(recentlyViewedIds)
    }

    val recentDishes =
        (recentlyUiState as? RecentlyUiState.Success)
            ?.dishes
            .orEmpty()
            .take(2)

    HomeScreen(
        searchQuery = query,
        onSearchQueryChange = menuViewModel::updateSearchQuery,
        categoryDishes = categoryDishes,
        onSearch = { menuViewModel.openFromHome(query = query); onGoMenu() },
        onOpenFilters = { menuViewModel.openFromHome(query = query, openFilters = true); onGoMenu() },
        onCategoryClick = { category -> menuViewModel.openFromHome(category = category); onGoMenu() },
        onGoToCart = onGoToCart,
        badgeCartCount = totalItemsCount,
        badgeCartText = if (totalItemsCount > 0) {
            totalItemsCount.toString()
        } else {
            ""
        },
        currentUser = userViewModel.currentUser,
        onExitApp = onExitApp,
        onGoViewedDish = onGoViewedDish,

        // Карточки появятся после подключения действий в AppNavHost.
        recentlyViewedDishes = if (
            onOpenRecentlyViewedDish != null &&
            onAddRecentlyViewedToCart != null
        ) recentDishes else emptyList(),

        onOpenRecentlyViewedDish = { dishId ->
            onOpenRecentlyViewedDish?.invoke(dishId)
        },

        onAddRecentlyViewedToCart = { dish ->
            onAddRecentlyViewedToCart?.invoke(dish)
        }
    )
}
