package com.example.catering.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.catering.navigation.AppNavHost
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.checkout.CheckoutViewModel
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.presentation.order.OrderViewModel
import com.example.catering.presentation.recently.RecentlyViewedViewModel
import com.example.catering.presentation.user.UserViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val recentlyViewModel: RecentlyViewedViewModel = hiltViewModel()

    val cartViewModel: CartViewModel = hiltViewModel()

    val favoriteViewModel: FavoriteViewModel = hiltViewModel()

    val orderViewModel: OrderViewModel = hiltViewModel()

    val userViewModel: UserViewModel = hiltViewModel()

    val checkoutViewModel: CheckoutViewModel = hiltViewModel()


    val currentUser = userViewModel.currentUser

    LaunchedEffect(currentUser) {
        // Синхронизируем Checkout с аккаунтом,
        // даже когда экран оформления не открыт.
        checkoutViewModel.syncAccount(currentUser)
    }


    AppNavHost(
        navController = navController,
        cartViewModel = cartViewModel,
        orderViewModel = orderViewModel,
        userViewModel = userViewModel,
        favoriteViewModel = favoriteViewModel,
        recentlyViewedViewModel = recentlyViewModel,
        checkoutViewModel = checkoutViewModel,
        context = context
    )
}
