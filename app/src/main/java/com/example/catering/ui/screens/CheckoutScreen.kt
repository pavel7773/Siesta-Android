
package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchBottom
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaSearchTop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Order
import com.example.catering.presentation.checkout.PromoCodeStatus
import com.example.catering.ui.components.common.*
import androidx.compose.material.icons.outlined.Schedule
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaGold
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaTextPrimary

private val CheckoutFieldColor = SiestaSearchMiddle
private val CheckoutMutedColor = SiestaNavUnselected

private val CheckoutGoldGradient = Brush.horizontalGradient(
    colors = listOf(
        SiestaCtaGoldLight,
        SiestaAmber,
        SiestaCtaGoldDark
    )
)

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    totalPrice: Long,
    onBack: () -> Unit,
    onConfirmOrder: (Order) -> Unit,
    isOrderSaving: Boolean,
    orderSavingError: String?,
    onGoMenu: () -> Unit,
    onOrderDataChanged: () -> Unit,
    nameUser: String,
    phoneUser: String,
    addressUser: String,
    deliveryTypeUser: String,
    commentUser: String,
    promoCode: String,
    discount: Long,
    promoCodeStatus: PromoCodeStatus,
    canConfirmOrder: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDeliveryTypeChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    isOnline: Boolean
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val finalPrice = maxOf(0L, totalPrice - discount)
    val isDelivery = deliveryTypeUser == "Доставка"
    var nameEdited by rememberSaveable { mutableStateOf(false) }
    var addressEdited by rememberSaveable { mutableStateOf(false) }

    val confirmEnabled =
        canConfirmOrder &&
                isOnline &&
                !isOrderSaving &&
                cartItems.isNotEmpty()

    if (isOrderSaving) {
        SiestaPage(texts.getString(R.string.ui_placing_your_order_203), onBack) {
            SiestaCard {
                Icon(Icons.Outlined.Schedule, null, tint = SiestaAmber)
                Text(texts.getString(R.string.ui_sending_your_order_204), color = SiestaTextPrimary, fontFamily = SiestaManrope, fontSize = 20.sp)
                SiestaBody(texts.getString(R.string.ui_this_may_take_a_moment_please_wait_for_the_result_205))
            }
            SiestaCard {
                Text(texts.getString(R.string.ui_your_order_206), color = SiestaTextPrimary, fontFamily = SiestaManrope)
                cartItems.forEach { item -> SiestaBody("${item.quantity} × ${item.dish.name}") }
                Text(texts.getString(R.string.ui_total_207, finalPrice), color = SiestaTextPrimary, fontFamily = SiestaManrope)
            }
            SiestaButton(texts.getString(R.string.ui_sending_208), {}, loading = true)
            SiestaMessage(texts.getString(R.string.ui_do_not_submit_your_order_again_209))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SiestaBgTop,
                        SiestaBgMiddle,
                        SiestaBgBottom
                    )
                )
            )
            .imePadding()
    ) {

        // ОБЩАЯ ШАПКА СИЕСТА
        ScreenHeader(
            title = texts.getString(R.string.ui_checkout_210),
            onBack = onBack,
            titleFontSize = 24.sp,
            compact = true,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp
            )
        )

        if (cartItems.isEmpty()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CheckoutCard(title = texts.getString(R.string.ui_your_cart_is_empty_13)) {
                    Text(
                        text = texts.getString(R.string.ui_add_dishes_from_the_menu_to_place_an_order_211),
                        fontFamily = SiestaManrope,
                        fontSize = 13.sp,
                        color = CheckoutMutedColor
                    )
                }

                CheckoutGoldButton(
                    text = texts.getString(R.string.ui_go_to_menu_212),
                    enabled = true,
                    loading = false,
                    onClick = onGoMenu
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                if (!orderSavingError.isNullOrBlank()) item(key = "order-error") {
                    SiestaMessage(orderSavingError, error = true)
                }
                if (!isOnline) item(key = "offline") {
                    SiestaMessage(texts.getString(R.string.ui_you_need_an_internet_connection_to_place_an_order_213), error = true)
                }
                // ВАШИ ДАННЫЕ
                item(key = "customer") {
                    CheckoutCard(title = texts.getString(R.string.ui_your_details_214)) {

                        CheckoutTextField(
                            value = nameUser,
                            onValueChange = {
                                nameEdited = true
                                onNameChange(it)
                                onOrderDataChanged()
                            },
                            label = texts.getString(R.string.ui_name_215),
                            isError = nameEdited && nameUser.isBlank(),
                            errorText = if (nameEdited && nameUser.isBlank()) texts.getString(R.string.ui_enter_your_name_14) else null
                        )

                        Spacer(Modifier.height(10.dp))

                        CheckoutTextField(
                            value = phoneUser,
                            onValueChange = {
                                onPhoneChange(it)
                                onOrderDataChanged()
                            },
                            label = texts.getString(R.string.ui_phone_number_216),
                            keyboardType = KeyboardType.Phone,
                            isError = phoneUser.isNotBlank() && phoneUser.count { it.isDigit() } < 10
                        )

                        if (
                            phoneUser.isNotBlank() &&
                            phoneUser.count { it.isDigit() } < 10
                        ) {
                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = texts.getString(R.string.ui_enter_a_valid_phone_number_23),
                                fontFamily = SiestaManrope,
                                fontSize = 13.sp,
                                color = SiestaError
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        CheckoutTextField(
                            value = commentUser,
                            onValueChange = {
                                onCommentChange(it)
                                onOrderDataChanged()
                            },
                            label = texts.getString(R.string.ui_order_notes_217),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }

                // ВАШ ЗАКАЗ
                item(key = "order") {
                    CheckoutCard(title = texts.getString(R.string.ui_your_order_206)) {

                        cartItems.forEachIndexed { index, cartItem ->

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${cartItem.dish.name} × ${cartItem.quantity}",
                                    modifier = Modifier.weight(1f),
                                    fontFamily = SiestaManrope,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = SiestaTextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${cartItem.dish.price.toLong() * cartItem.quantity} ₽",
                                    fontFamily = SiestaManrope,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SiestaTextPrimary
                                )
                            }

                            if (index != cartItems.lastIndex) {
                                Spacer(Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            SiestaNavOutline.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                )

                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // СПОСОБ ПОЛУЧЕНИЯ
                item(key = "delivery") {
                    CheckoutCard(title = texts.getString(R.string.ui_order_type_218)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            CheckoutDeliveryOption(
                                title = texts.getString(R.string.ui_pickup_65),
                                selected = deliveryTypeUser == "Самовывоз",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDeliveryTypeChange("Самовывоз")
                                    onOrderDataChanged()
                                }
                            )

                            CheckoutDeliveryOption(
                                title = texts.getString(R.string.ui_delivery_16),
                                selected = isDelivery,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDeliveryTypeChange("Доставка")
                                    onOrderDataChanged()
                                }
                            )
                        }

                        if (isDelivery) {
                            Spacer(Modifier.height(12.dp))

                            CheckoutTextField(
                                value = addressUser,
                                onValueChange = {
                                    addressEdited = true
                                    onAddressChange(it)
                                    onOrderDataChanged()
                                },
                                label = texts.getString(R.string.ui_delivery_address_157),
                                isError = addressEdited && addressUser.isBlank(),
                                errorText = if (addressEdited && addressUser.isBlank()) texts.getString(R.string.ui_enter_your_delivery_address_17) else null,
                                minLines = 2,
                                maxLines = 3
                            )
                        }
                    }
                }

                // ПРОМОКОД
                item(key = "promo") {
                    CheckoutCard(title = texts.getString(R.string.ui_promo_code_219)) {

                        CheckoutTextField(
                            value = promoCode,
                            onValueChange = {
                                onPromoCodeChange(it)
                                onOrderDataChanged()
                            },
                            label = texts.getString(R.string.ui_enter_a_promo_code_7),
                            isError = promoCodeStatus is PromoCodeStatus.Error
                        )

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(50))
                                .border(
                                    width = 1.dp,
                                    color = SiestaAmber,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable(
                                    enabled = promoCode.isNotBlank(),
                                    role = Role.Button,
                                    onClick = onApplyPromoCode
                                )
                                .padding(
                                    horizontal = 22.dp,
                                    vertical = 12.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = texts.getString(R.string.ui_apply_220),
                                fontFamily = SiestaManrope,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (promoCode.isNotBlank()) {
                                    SiestaAmber
                                } else {
                                    CheckoutMutedColor
                                }
                            )
                        }

                        when (val status = promoCodeStatus) {

                            PromoCodeStatus.Idle -> Unit

                            is PromoCodeStatus.Applied -> {
                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = texts.uiMessage(status.message),
                                    fontFamily = SiestaManrope,
                                    fontSize = 12.sp,
                                    color = SiestaAmber
                                )
                            }

                            is PromoCodeStatus.Error -> {
                                Spacer(Modifier.height(10.dp))

                                SiestaMessage(texts.uiMessage(status.message), error = true)
                            }
                        }
                    }
                }

                item(key = "bottom-space") {
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ЗАКРЕПЛЁННАЯ НИЖНЯЯ ПАНЕЛЬ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiestaBackgroundDark)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                if (discount > 0L) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = texts.getString(R.string.ui_subtotal_221),
                            fontFamily = SiestaManrope,
                            fontSize = 12.sp,
                            color = CheckoutMutedColor
                        )

                        Text(
                            text = "$totalPrice ₽",
                            fontFamily = SiestaManrope,
                            fontSize = 12.sp,
                            color = CheckoutMutedColor,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = texts.getString(R.string.ui_discount_222),
                            fontFamily = SiestaManrope,
                            fontSize = 12.sp,
                            color = CheckoutMutedColor
                        )

                        Text(
                            text = "−$discount ₽",
                            fontFamily = SiestaManrope,
                            fontSize = 12.sp,
                            color = SiestaAmber
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = texts.getString(R.string.ui_total_223),
                        fontFamily = SiestaManrope,
                        fontSize = 15.sp,
                        color = SiestaTextPrimary
                    )

                    Text(
                        text = "$finalPrice ₽",
                        fontFamily = SiestaManrope,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiestaTextPrimary
                    )
                }

                SiestaButton(
                    text = if (isOrderSaving) {
                        texts.getString(R.string.ui_placing_order_224)
                    } else {
                        texts.getString(R.string.ui_place_order_225)
                    },
                    enabled = confirmEnabled,
                    loading = isOrderSaving,
                    onClick = {
                        val order = Order(
                            0,
                            nameUser,
                            phoneUser,
                            commentUser,
                            deliveryTypeUser,
                            addressUser.trim(),
                            cartItems,
                            finalPrice
                        )

                        onConfirmOrder(order)
                    }
                )
            }
        }
    }
}

// ТЁМНАЯ КАРТОЧКА СЕКЦИИ

@Composable
private fun CheckoutCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SiestaCardTop,
                        SiestaBgMiddle
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SiestaAmber.copy(alpha = 0.34f),
                shape = shape
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SiestaAmber)
            )

            Text(
                text = title,
                fontFamily = SiestaManrope,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = SiestaTextPrimary
            )
        }

        Spacer(Modifier.height(14.dp))

        content()
    }
}

// ОБЩЕЕ ОФОРМЛЕНИЕ ПОЛЕЙ


@Composable
private fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1,
    isError: Boolean = false,
    errorText: String? = null
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val singleLine = minLines == 1 && maxLines == 1
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // ПОДПИСЬ НАД ПОЛЕМ
        Text(
            text = label,
            fontFamily = SiestaManrope,
            fontSize = 12.sp,
            color = SiestaNavUnselected
        )

        Spacer(Modifier.height(7.dp))

        // ТЁМНОЕ ПОЛЕ С ГРАДИЕНТОМ
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) {
                        Modifier.heightIn(min = 56.dp)
                    } else {
                        Modifier.heightIn(min = 104.dp)
                    }
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SiestaSearchTop,
                            SiestaSearchBottom
                        )
                    )
                ),
            singleLine = singleLine,
            isError = isError,
            supportingText = if (errorText == null) null else { { SiestaBody(errorText, error = true) } },
            minLines = minLines,
            maxLines = maxLines,
            placeholder = {
                Text(
                    text = when (label) {
                        texts.getString(R.string.ui_phone_number_216) ->
                            "+7 (900) 000-00-00"
                        texts.getString(R.string.ui_name_215) ->
                            texts.getString(R.string.ui_enter_your_name_14)
                        else -> label
                    },
                    fontFamily = SiestaManrope,
                    fontSize = 13.sp,
                    color = SiestaNavUnselected
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SiestaManrope,
                fontSize = 14.sp,
                color = SiestaTextPrimary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SiestaTextPrimary,
                unfocusedTextColor = SiestaTextPrimary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorBorderColor = SiestaError,
                errorTextColor = SiestaTextPrimary,
                errorContainerColor = Color.Transparent,
                focusedBorderColor = SiestaAmber,
                unfocusedBorderColor = SiestaNavOutline,
                cursorColor = SiestaAmber,
                focusedPlaceholderColor = SiestaNavUnselected,
                unfocusedPlaceholderColor = SiestaNavUnselected
            )
        )
    }
}


// САМОВЫВОЗ / ДОСТАВКА

@Composable
private fun CheckoutDeliveryOption(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(shape)
            .background(
                if (selected) {
                    SiestaAmber.copy(alpha = 0.15f)
                } else {
                    CheckoutFieldColor
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    SiestaAmber
                } else {
                    SiestaNavOutline
                },
                shape = shape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    width = 2.dp,
                    color = if (selected) {
                        SiestaAmber
                    } else {
                        CheckoutMutedColor
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(
                            SiestaAmber,
                            CircleShape
                        )
                )
            }
        }

        Text(
            text = title,
            fontFamily = SiestaManrope,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Medium
            },
            color = if (selected) {
                SiestaGold
            } else {
                SiestaTextPrimary
            }
        )
    }
}

// ЗОЛОТАЯ КНОПКА

@Composable
private fun CheckoutGoldButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(shape)
            .background(
                if (enabled) {
                    CheckoutGoldGradient
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            SiestaSearchTop,
                            SiestaSearchMiddle
                        )
                    )
                }
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                start = 17.dp,
                end = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (enabled) {
                SiestaBackgroundDark
            } else {
                SiestaNavUnselected
            }
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SiestaCtaGoldDark.copy(alpha = 85 / 255f))
                .border(
                    width = 1.dp,
                    color = SiestaCtaGoldLight.copy(alpha = 170 / 255f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    color = SiestaTextPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SiestaTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
