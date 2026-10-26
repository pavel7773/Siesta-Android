package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import com.example.catering.ui.screens.OrderSuccessScreen

@Composable
fun OrderSuccessRoute(
    onGoMenu: () -> Unit,
    onGoToOrdersHistory: () -> Unit,
    onGoHome: () -> Unit
) {
    OrderSuccessScreen(
        onGoMenu = onGoMenu,
        onGoToOrdersHistory = onGoToOrdersHistory,
        onGoHome = onGoHome
    )
}
