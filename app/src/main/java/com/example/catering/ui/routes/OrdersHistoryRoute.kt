package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.order.OrderViewModel
import com.example.catering.ui.screens.OrdersHistoryScreen

@Composable
fun OrdersHistoryRoute(
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onGoCart: () -> Unit,
    onGoMenu: () -> Unit
) {

    val cartItemCount by cartViewModel.totalItemsCount.collectAsStateWithLifecycle()
    val isRefreshing by orderViewModel.isRefreshingOrders.collectAsStateWithLifecycle()
    val orders by orderViewModel.ordersState.collectAsStateWithLifecycle()
    val orderActionStatus by orderViewModel.orderActionStatus.collectAsStateWithLifecycle()

    OrdersHistoryScreen(
        onBack = onBack,
        orders = orders,
        isRefreshing = isRefreshing,
        cartItemCount = cartItemCount,
        onGoCart = onGoCart,
        onRepeatOrder = { cartViewModel.repeatOrder(it) },
        onGoMenu = onGoMenu,
        onCancelOrder = { orderViewModel.cancelOrder(it) },
        orderActionStatus = orderActionStatus,
        clearOrderActionStatus = {
            orderViewModel.clearOrderActionStatus()
        },
        onRefreshOrders = {
            orderViewModel.refreshOrders()
        }
    )
}