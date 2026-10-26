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
import com.example.catering.presentation.checkout.CheckoutViewModel
import com.example.catering.presentation.checkout.PromoCodeStatus
import com.example.catering.presentation.order.OrderSavingStatus
import com.example.catering.presentation.order.OrderViewModel
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.CheckoutScreen

@Composable
fun CheckoutRoute(
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onGoMenu: () -> Unit,
    onGoLogin: () -> Unit,
    onOrderSuccess: () -> Unit

) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val cartEntries by cartViewModel.cartEntriesState.collectAsStateWithLifecycle()
    val orderSavingStatus by orderViewModel.orderSavingStatus.collectAsStateWithLifecycle()
    val nameUser by checkoutViewModel.name.collectAsStateWithLifecycle()
    val phoneUser by checkoutViewModel.phone.collectAsStateWithLifecycle()
    val addressUser by checkoutViewModel.address.collectAsStateWithLifecycle()
    val deliveryTypeUser by checkoutViewModel.deliveryType.collectAsStateWithLifecycle()
    val commentUser by checkoutViewModel.comment.collectAsStateWithLifecycle()
    val promoCode by checkoutViewModel.promoCode.collectAsStateWithLifecycle()
    val discount by checkoutViewModel.discount.collectAsStateWithLifecycle()
    val promoCodeStatus by checkoutViewModel.promoCodeStatus.collectAsStateWithLifecycle()
    val canConfirmOrder by checkoutViewModel.canConfirmOrder.collectAsStateWithLifecycle()
    val isOnline by checkoutViewModel.isOnline.collectAsStateWithLifecycle()
    val isOrderSaving = orderSavingStatus is OrderSavingStatus.Loading
    val orderSavingError = when (val status = orderSavingStatus) {
        is OrderSavingStatus.Error -> status.message
        else -> null
    }
    val currentUser = userViewModel.currentUser

    val cartItemsViewModel: CartItemsViewModel = hiltViewModel()

    val cartUiState by cartItemsViewModel.cartItemsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(cartEntries) {
        cartItemsViewModel.loadCartItems(cartEntries)
    }

    LaunchedEffect(orderSavingStatus) { // Этот блок запускается каждый раз, когда меняется orderSavingStatus
        when (orderSavingStatus) {

            is OrderSavingStatus.Success -> {
                cartViewModel.clearCart()
                checkoutViewModel.resetAfterOrder()
                orderViewModel.resetSavingStatus()
                onOrderSuccess()

            }

            OrderSavingStatus.Idle -> Unit

            is OrderSavingStatus.Error -> Unit
            OrderSavingStatus.Loading -> Unit
        }
    }
    val promoCodeForOrder =
        if (promoCodeStatus is PromoCodeStatus.Applied) {
            promoCode
        } else {
            null
        }

    when (val state = cartUiState) {

        is CartUiState.Loading -> {
            LoadingStateContent(message = texts.getString(R.string.ui_loading_142))

        }

        is CartUiState.Empty -> {

            LaunchedEffect(Unit) {
                checkoutViewModel.onOrderAmountChanged(0L)
            }

            CheckoutScreen(
                cartItems = emptyList(),
                totalPrice = 0,
                onBack = onBack,
                onConfirmOrder = confirmOrder@{ order ->
                    if (!isOnline) return@confirmOrder
                    if (isOrderSaving) return@confirmOrder
                    if (currentUser == null) {
                        onGoLogin()
                    } else {
                        orderViewModel.addOrder(
                            order = order,
                            userId = currentUser.id,
                            promoCode = promoCodeForOrder
                        )
                    }

                },
                isOrderSaving = isOrderSaving,
                orderSavingError = orderSavingError,
                onGoMenu = onGoMenu,
                onOrderDataChanged = { orderViewModel.clearOrderSavingError() },
                nameUser = nameUser,
                phoneUser = phoneUser,
                addressUser = addressUser,
                deliveryTypeUser = deliveryTypeUser,
                commentUser = commentUser,
                promoCode = promoCode,
                discount = discount,
                promoCodeStatus = promoCodeStatus,
                canConfirmOrder = canConfirmOrder,
                onNameChange = { checkoutViewModel.updateName(it) },
                onPhoneChange = { checkoutViewModel.updatePhone(it) },
                onAddressChange = { checkoutViewModel.updateAddress(it) },
                onDeliveryTypeChange = { checkoutViewModel.updateDeliveryType(it) },
                onCommentChange = { checkoutViewModel.updateComment(it) },
                onPromoCodeChange = { checkoutViewModel.updatePromoCode(it) },
                onApplyPromoCode = { checkoutViewModel.applyPromoCode(0) },
                isOnline = isOnline

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

            LaunchedEffect(totalPrice) {
                checkoutViewModel.onOrderAmountChanged(
                    orderAmount = totalPrice
                )
            }

            CheckoutScreen(
                cartItems = cartItems,
                totalPrice = totalPrice,
                onBack = onBack,
                onConfirmOrder = confirmOrder@{ order ->
                    if (!isOnline) return@confirmOrder
                    if (isOrderSaving) return@confirmOrder
                    if (currentUser == null) {
                        onGoLogin()
                    } else {
                        orderViewModel.addOrder(
                            order = order,
                            userId = currentUser.id,
                            promoCode = promoCodeForOrder
                        )
                    }

                },
                isOrderSaving = isOrderSaving,
                orderSavingError = orderSavingError,
                onGoMenu = onGoMenu,
                onOrderDataChanged = { orderViewModel.clearOrderSavingError() },
                nameUser = nameUser,
                phoneUser = phoneUser,
                addressUser = addressUser,
                deliveryTypeUser = deliveryTypeUser,
                commentUser = commentUser,
                promoCode = promoCode,
                discount = discount,
                promoCodeStatus = promoCodeStatus,
                canConfirmOrder = canConfirmOrder,
                onNameChange = { checkoutViewModel.updateName(it) },
                onPhoneChange = { checkoutViewModel.updatePhone(it) },
                onAddressChange = { checkoutViewModel.updateAddress(it) },
                onDeliveryTypeChange = { checkoutViewModel.updateDeliveryType(it) },
                onCommentChange = { checkoutViewModel.updateComment(it) },
                onPromoCodeChange = { checkoutViewModel.updatePromoCode(it) },
                onApplyPromoCode = { checkoutViewModel.applyPromoCode(totalPrice) },
                isOnline = isOnline

            )
        }
    }
}