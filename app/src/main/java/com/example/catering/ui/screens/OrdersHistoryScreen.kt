
package com.example.catering.ui.screens


import com.example.catering.presentation.localization.uiMessage
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaCompleted
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaGoldSurface
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSuccess
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.R
import com.example.catering.domain.model.Order
import com.example.catering.domain.model.OrderStatus
import com.example.catering.presentation.order.OrderActionStatus
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.components.state.LoadingCards
import com.example.catering.ui.components.common.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.catering.ui.components.state.EmptyStateContent
import com.example.catering.ui.theme.SiestaManrope
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HistoryGold = SiestaAmber
private val HistoryCream = SiestaTextPrimary
private val HistoryMuted = SiestaNavUnselected
private val HistoryOutline = SiestaNavOutline

private val HistoryDisplayFont = FontFamily(
    Font(R.font.cormorant_garamond_semibold)
)

private val HistoryCardGradient = Brush.verticalGradient(
    listOf(
        SiestaCardTop,
        SiestaCardMiddle,
        SiestaBgMiddle
    )
)

private val HistoryGoldGradient = Brush.horizontalGradient(
    listOf(
        SiestaCtaGoldLight,
        SiestaAmber,
        SiestaCtaGoldDark
    )
)

@Composable
fun OrdersHistoryScreen(
    onBack: () -> Unit,
    orders: List<Order>,
    onRepeatOrder: (Order) -> Unit,
    onGoMenu: () -> Unit,
    onCancelOrder: (Order) -> Unit,
    orderActionStatus: OrderActionStatus,
    onRefreshOrders: () -> Unit,
    clearOrderActionStatus: () -> Unit,
    isRefreshing: Boolean = false,
    onGoCart: (() -> Unit)? = null,
    cartItemCount: Int = 0
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val snackbarHostState = remember { SnackbarHostState() }
    var repeatOrder by remember { mutableStateOf<Order?>(null) }
    var cancelOrder by remember { mutableStateOf<Order?>(null) }
    repeatOrder?.let { order ->
        OrderConfirmation(order, cancel = false, cartItemCount = cartItemCount,
            onDismiss = { repeatOrder = null }, onConfirm = {
                repeatOrder = null
                onRepeatOrder(order)
            })
    }
    cancelOrder?.let { order ->
        OrderConfirmation(order, cancel = true, cartItemCount = cartItemCount,
            onDismiss = { cancelOrder = null }, onConfirm = {
                cancelOrder = null
                onCancelOrder(order)
            })
    }

    val sortedOrders = remember(orders) {
        orders.sortedByDescending { it.createdAt }
    }

    LaunchedEffect(Unit) {
        onRefreshOrders()
    }

    LaunchedEffect(orderActionStatus) {
        when (val status = orderActionStatus) {
            is OrderActionStatus.Success -> {
                clearOrderActionStatus()
                onRefreshOrders()
                snackbarHostState.showSnackbar(texts.uiMessage(status.message))
            }

            is OrderActionStatus.Error -> {
                clearOrderActionStatus()
                snackbarHostState.showSnackbar(texts.uiMessage(status.message))
            }

            OrderActionStatus.Idle -> Unit
        }
    }

    Scaffold(
        containerColor = SiestaBgMiddle,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SiestaBgTop,
                            SiestaBgMiddle,
                            SiestaBgBottom
                        )
                    )
                )
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ШАПКА
                item(key = "header") {
                    ScreenHeader(
                        title = texts.getString(R.string.ui_order_history_359),
                        trailing = onGoCart?.let { { SiestaCartButton(cartItemCount, it) } },
                        onBack = onBack,
                        compact = true,
                        centerTitle = true,
                        titleFontSize = 24.sp
                    )
                }

                if (isRefreshing && sortedOrders.isEmpty()) {
                    item(key = "loading") {
                        SiestaHeading(texts.getString(R.string.ui_your_orders_360))
                        Spacer(Modifier.height(16.dp))
                        LoadingCards(texts.getString(R.string.ui_loading_order_history_361))
                    }
                } else if (sortedOrders.isEmpty()) {

                    item(key = "empty") {
                        EmptyStateContent(
                            title = texts.getString(R.string.ui_no_orders_yet_362),
                            message = texts.getString(R.string.ui_place_your_first_order_from_the_menu_nand_it_will_appear_h_363),
                            buttonText = texts.getString(R.string.ui_go_to_menu_212),
                            onButtonClick = onGoMenu
                        )
                    }

                } else {
                    // КАРТОЧКИ ЗАКАЗОВ
                    itemsIndexed(
                        items = sortedOrders,
                        key = { _, order -> order.id }
                    ) { index, order ->

                        HistoryOrderCard(
                            order = order,
                            initiallyExpanded = false,
                            onCancel = {
                                cancelOrder = order
                            },
                            onRepeat = {
                                repeatOrder = order
                            }
                        )
                    }
                }
            }
        }
    }
}
// КАРТОЧКА ЗАКАЗА

@Composable
private fun HistoryOrderCard(
    order: Order,
    initiallyExpanded: Boolean,
    onCancel: () -> Unit,
    onRepeat: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var expanded by rememberSaveable(order.id) {
        mutableStateOf(initiallyExpanded)
    }

    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HistoryCardGradient)
            .border(
                width = 1.dp,
                color = if (expanded) {
                    HistoryGold.copy(alpha = 0.55f)
                } else {
                    HistoryOutline
                },
                shape = shape
            )
            .padding(18.dp)
    ) {

        // ЗАГОЛОВОК КАРТОЧКИ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClick = {
                        expanded = !expanded
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = texts.getString(R.string.ui_order_364, order.id),
                    fontFamily = HistoryDisplayFont,
                    fontSize = 32.sp,
                    lineHeight = 37.sp,
                    color = HistoryCream
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                historyStatusColor(order.statusOrder),
                                CircleShape
                            )
                    )

                    Text(
                        text = historyStatusText(order.statusOrder),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        color = HistoryMuted
                    )
                }
            }

            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                },
                contentDescription = if (expanded) {
                    texts.getString(R.string.ui_collapse_order_365)
                } else {
                    texts.getString(R.string.ui_expand_order_366)
                },
                tint = HistoryGold,
                modifier = Modifier.size(27.dp)
            )
        }

        if (expanded) {

            Spacer(Modifier.height(18.dp))

            HistoryOrderProgress(order = order)

            Spacer(Modifier.height(18.dp))

            HorizontalDivider(
                color = HistoryOutline.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            HistoryInfoRow(
                icon = Icons.Filled.DateRange,
                label = texts.getString(R.string.ui_date_367),
                value = formatOrderDate(order.createdAt)
            )

            HistoryInfoRow(
                icon = Icons.Filled.Info,
                label = texts.getString(R.string.ui_amount_368),
                value = "${order.totalAmount} ₽"
            )

            HistoryInfoRow(
                icon = Icons.Filled.Place,
                label = texts.getString(R.string.ui_order_type_218),
                value = order.deliveryType
            )

            if (
                order.deliveryType == "Доставка" &&
                order.address.isNotBlank()
            ) {
                HistoryInfoRow(
                    icon = Icons.Filled.Place,
                    label = texts.getString(R.string.ui_delivery_address_157),
                    value = order.address
                )
            }

            HistoryInfoRow(
                icon = Icons.Filled.ShoppingCart,
                label = texts.getString(R.string.ui_order_items_369),
                value = order.products.joinToString("\n") { item ->
                    "${item.dish.name} × ${item.quantity}"
                }.ifBlank { texts.getString(R.string.ui_no_item_details_available_370) }
            )

            HistoryInfoRow(
                icon = Icons.Filled.Person,
                label = texts.getString(R.string.ui_recipient_name_371),
                value = order.name
            )

            HistoryInfoRow(
                icon = Icons.Filled.Phone,
                label = texts.getString(R.string.ui_phone_325),
                value = order.phone,
                showDivider = false
            )

            Spacer(Modifier.height(20.dp))

            // ОТМЕНА ДОСТУПНА ТОЛЬКО ДЛЯ ПРИНЯТОГО ЗАКАЗА
            if (order.statusOrder == OrderStatus.ACCEPTED) {

                HistoryActionButton(
                    text = texts.getString(R.string.ui_cancel_order_372),
                    primary = false,
                    icon = Icons.Filled.Close,
                    onClick = onCancel
                )

                Spacer(Modifier.height(10.dp))
            }

            HistoryActionButton(
                text = texts.getString(R.string.ui_reorder_373),
                primary = true,
                icon = Icons.Filled.Refresh,
                onClick = onRepeat
            )
        }
    }
}



@Composable
private fun HistoryOrderProgress(order: Order) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources


    val currentStep = when (order.statusOrder) {
        OrderStatus.ACCEPTED -> 0
        OrderStatus.COOKING -> 1
        OrderStatus.READY -> 2
        OrderStatus.COMPLETED -> 3
        OrderStatus.CANCELED -> -1
    }

    if (currentStep == -1) {
        Text(
            text = texts.getString(R.string.ui_order_canceled_70),
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            color = SiestaError
        )
        return
    }

    val isDelivery = order.deliveryType == "Доставка"

    val stages: List<Pair<String, ImageVector>> =
        if (isDelivery) {
            val listOf = listOf(
                texts.getString(R.string.ui_accepted_374) to Icons.Outlined.CheckCircle,
                texts.getString(R.string.ui_preparing_375) to Icons.Outlined.Restaurant,
                texts.getString(R.string.ui_ready_for_ndelivery_376) to Icons.Outlined.ShoppingBag,
                texts.getString(R.string.ui_delivered_377) to Icons.Outlined.Home
            )
            listOf
        } else {
            listOf(
                texts.getString(R.string.ui_accepted_374) to Icons.Outlined.CheckCircle,
                texts.getString(R.string.ui_preparing_375) to Icons.Outlined.Restaurant,
                texts.getString(R.string.ui_ready_for_npickup_378) to Icons.Outlined.RoomService,
                texts.getString(R.string.ui_collected_379) to Icons.Outlined.ShoppingBag
            )
        }

    val statusDescription = when (order.statusOrder) {
        OrderStatus.ACCEPTED ->
            texts.getString(R.string.ui_your_order_has_been_accepted_380)

        OrderStatus.COOKING ->
            texts.getString(R.string.ui_preparing_your_order_381)

        OrderStatus.READY ->
            if (isDelivery)
                texts.getString(R.string.ui_your_order_is_ready_for_delivery_382)
            else
                texts.getString(R.string.ui_your_order_is_ready_for_pickup_383)

        OrderStatus.COMPLETED ->
            if (isDelivery)
                texts.getString(R.string.ui_order_delivered_384)
            else
                texts.getString(R.string.ui_order_collected_385)

        OrderStatus.CANCELED ->
            texts.getString(R.string.ui_order_canceled_70)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SiestaCardMiddle)
            .border(
                1.dp,
                HistoryOutline.copy(alpha = 0.6f),
                RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 16.dp
            )
    ) {

        Text(
            text = texts.getString(R.string.ui_order_progress_386),
            fontFamily = SiestaManrope,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = HistoryCream,
            modifier = Modifier.padding(start = 6.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {

            stages.forEachIndexed { index, stage ->

                val isCurrent = index == currentStep
                val isCompleted = index < currentStep

                val circleColor = when {
                    isCurrent -> HistoryGold
                    isCompleted -> SiestaGoldSurface
                    else -> SiestaCardTop
                }

                val iconColor = when {
                    isCurrent -> SiestaCardMiddle
                    isCompleted -> HistoryGold
                    else -> HistoryMuted
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val halfLine = (maxWidth - 34.dp) / 2

                        // Соединение с предыдущим этапом
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .width(halfLine)
                                    .height(2.dp)
                                    .background(
                                        if (index <= currentStep)
                                            HistoryGold
                                        else
                                            HistoryOutline
                                    )
                            )
                        }

                        // Соединение со следующим этапом
                        if (index < stages.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(halfLine)
                                    .height(2.dp)
                                    .background(
                                        if (index < currentStep)
                                            HistoryGold
                                        else
                                            HistoryOutline
                                    )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .border(
                                    width = 1.dp,
                                    color = if (
                                        isCurrent || isCompleted
                                    ) HistoryGold
                                    else HistoryOutline,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = stage.second,
                                contentDescription = stage.first,
                                tint = iconColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = stage.first,
                        fontFamily = SiestaManrope,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = if (
                            isCurrent || isCompleted
                        ) HistoryGold else HistoryMuted,
                        fontWeight = if (isCurrent)
                            FontWeight.SemiBold
                        else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        minLines = 2
                    )
                }
            }
        }

        Spacer(Modifier.height(17.dp))

        Text(
            text = statusDescription,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = SiestaManrope,
            fontSize = 12.sp,
            color = HistoryMuted,
            textAlign = TextAlign.Center
        )
    }
}


// СТРОКА ИНФОРМАЦИИ

@Composable
private fun HistoryInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HistoryGold,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(21.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = label,
                fontFamily = SiestaManrope,
                fontSize = 12.sp,
                color = HistoryMuted
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = value,
                fontFamily = SiestaManrope,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = HistoryCream
            )
        }
    }

    if (showDivider) {
        Spacer(Modifier.height(12.dp))

        HorizontalDivider(
            modifier = Modifier.padding(start = 35.dp),
            color = HistoryOutline.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(12.dp))
    }
}

// КНОПКИ ДЕЙСТВИЙ

@Composable
private fun HistoryActionButton(
    text: String,
    primary: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                if (primary) {
                    HistoryGoldGradient
                } else {
                    Brush.verticalGradient(
                        listOf(
                            SiestaCardTop,
                            SiestaCardMiddle
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (primary) {
                    SiestaCtaGoldLight
                } else {
                    HistoryGold.copy(alpha = 0.8f)
                },
                shape = shape
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (primary) {
                SiestaBackgroundDark
            } else {
                HistoryGold
            },
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (primary) {
                SiestaBackgroundDark
            } else {
                HistoryGold
            }
        )

        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = if (primary) {
                SiestaBackgroundDark
            } else {
                HistoryGold
            },
            modifier = Modifier.size(19.dp)
        )
    }
}

// ПОДПИСИ СТАТУСОВ

@Composable
private fun historyStatusText(
    status: OrderStatus
): String {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources
    return when (status) {
        OrderStatus.ACCEPTED -> texts.getString(R.string.ui_order_accepted_387)
        OrderStatus.COOKING -> texts.getString(R.string.ui_preparing_375)
        OrderStatus.READY -> texts.getString(R.string.ui_ready_388)
        OrderStatus.COMPLETED -> texts.getString(R.string.ui_completed_389)
        OrderStatus.CANCELED -> texts.getString(R.string.ui_canceled_390)
    }
}

// ЦВЕТ СТАТУСА

private fun historyStatusColor(
    status: OrderStatus
): Color {
    return when (status) {
        OrderStatus.ACCEPTED -> HistoryGold
        OrderStatus.COOKING -> SiestaAmber
        OrderStatus.READY -> SiestaSuccess
        OrderStatus.COMPLETED -> SiestaCompleted
        OrderStatus.CANCELED -> SiestaError
    }
}

// ФОРМАТИРОВАНИЕ ДАТЫ

fun formatOrderDate(createdAt: Long): String {
    return if (createdAt <= 0L) {
        "Дата неизвестна"
    } else {
        Instant.ofEpochMilli(createdAt)
            .atZone(ZoneId.systemDefault())
            .format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            )
    }
}


@Composable
private fun OrderConfirmation(order: Order, cancel: Boolean, cartItemCount: Int,
                              onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.clip(RoundedCornerShape(24.dp)).background(SiestaScreenBrush)
            .border(1.dp, SiestaNavOutline, RoundedCornerShape(24.dp))
            .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SiestaHeading(if (cancel) texts.getString(R.string.ui_cancel_order_392, order.id) else if (cartItemCount > 0) texts.getString(R.string.ui_replace_your_cart_393) else texts.getString(R.string.ui_reorder_394))
            SiestaBody(if (cancel) texts.getString(R.string.ui_once_confirmed_a_cancellation_request_will_be_sent_to_the_395)
                else if (cartItemCount > 0) texts.getString(R.string.ui_items_in_your_cart_they_will_be_replaced_with_available_di_396, cartItemCount, order.id)
                else texts.getString(R.string.ui_available_dishes_from_order_will_be_added_to_your_cart_at_397, order.id))
            if (cancel) SiestaMessage(texts.getString(R.string.ui_you_can_cancel_while_the_order_status_allows_it_398), error = true)
            else SiestaCard {
                Text(texts.getString(R.string.ui_your_order_206), color = SiestaTextPrimary, fontFamily = SiestaManrope)
                order.products.forEach { item -> SiestaBody("${item.quantity} × ${item.dish.name}") }
                Text(texts.getString(R.string.ui_previous_order_total_399, order.totalAmount), color = SiestaTextPrimary, fontFamily = SiestaManrope)
            }
            SiestaButton(if (cancel) texts.getString(R.string.ui_yes_cancel_400) else if (cartItemCount > 0) texts.getString(R.string.ui_replace_and_continue_401) else texts.getString(R.string.ui_add_to_cart_117),
                onConfirm, destructive = cancel)
            SiestaButton(if (cancel) texts.getString(R.string.ui_keep_order_402) else texts.getString(R.string.ui_keep_cart_403), onDismiss, secondary = true)
        }
    }
}
