package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.CartItem
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    totalPrice: Long,
    favoriteDishIds: List<Int>,
    onToggleFavorite: (Int, Boolean) -> Unit,
    onGoMenu: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val count = cartItems.sumOf { it.quantity }

    Column(
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
            .padding(horizontal = 16.dp)
    ) {

        ScreenHeader(
            title = if (cartItems.isEmpty()) texts.getString(R.string.ui_cart_181) else texts.getString(R.string.ui_your_selection_182),
            onBack = onBack,
            compact = true,
            centerTitle = true,
            titleFontSize = 24.sp
        )
        if (cartItems.isNotEmpty()) {
        Text(
            text = texts.getQuantityString(R.plurals.dish_count, count, count),
            fontFamily = SiestaManrope,
            fontSize = 16.sp,
            color = SiestaNavUnselected
        )
            }

        Spacer(Modifier.height(14.dp))


        if (cartItems.isEmpty()) {
            SiestaEmptyCart(
                onGoMenu = onGoMenu,
                modifier = Modifier.weight(1f)
            )
        } else {
            // КАРТОЧКИ БЛЮД
            CartSection(
                cartItems = cartItems,
                onIncrease = onIncrease,
                onDecrease = onDecrease,
                onRemove = onRemove,
                favoriteDishIds = favoriteDishIds,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.weight(1f)
            )

            // ИТОГО И ОФОРМЛЕНИЕ
            CartBottomBar(
                totalPrice = totalPrice,
                onOrderClick = onCheckout
            )
        }
    }
}

@Composable
fun CartBottomBar(
    totalPrice: Long,
    onOrderClick: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 14.dp)
            .height(54.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SiestaCtaGoldLight,
                        SiestaAmber,
                        SiestaCtaGoldDark
                    )
                )
            )
            .clickable(
                role = Role.Button,
                onClick = onOrderClick
            )
            .padding(start = 16.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texts.getString(R.string.ui_checkout_183, totalPrice),
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SiestaBackgroundDark
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SiestaCtaGoldDark.copy(alpha = 68 / 255f))
                .border(
                    width = 1.dp,
                    color = SiestaCtaGoldLight.copy(alpha = 170 / 255f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = SiestaTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
private fun SiestaEmptyCart(
    onGoMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = texts.getString(R.string.ui_start_with_a_favorite_184),
            fontFamily = SiestaManrope,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = SiestaTextPrimary
        )

        val shape = RoundedCornerShape(20.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 156.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SiestaCardTop,
                            SiestaBackgroundDark
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = SiestaNavOutline,
                    shape = shape
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = SiestaAmber,
                modifier = Modifier.size(25.dp)
            )

            Text(
                text = texts.getString(R.string.ui_your_cart_is_empty_185),
                fontFamily = SiestaManrope,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SiestaTextPrimary
            )

            Text(
                text = texts.getString(R.string.ui_add_dishes_from_the_menu_they_will_appear_here_186),
                fontFamily = SiestaManrope,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                color = SiestaNavUnselected
            )
        }

        Spacer(Modifier.height(1.dp))

        val buttonShape = RoundedCornerShape(50)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(buttonShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SiestaCtaGoldLight,
                            SiestaAmber,
                            SiestaCtaGoldDark
                        )
                    )
                )
                .clickable(
                    role = Role.Button,
                    onClick = onGoMenu
                )
                .padding(
                    start = 18.dp,
                    end = 7.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texts.getString(R.string.ui_open_menu_187),
                modifier = Modifier.weight(1f),
                fontFamily = SiestaManrope,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SiestaBackgroundDark
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SiestaCtaGoldDark.copy(alpha = 102 / 255f))
                    .border(
                        width = 1.dp,
                        color = SiestaCtaGoldLight.copy(alpha = 170 / 255f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
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

