
package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchBottom
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.catering.domain.model.Dish
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.components.common.SiestaDishCard
import com.example.catering.ui.components.common.SiestaCartButton
import com.example.catering.ui.components.state.EmptyStateContent
import com.example.catering.ui.theme.SiestaManrope

private val FavoriteGold = SiestaAmber
private val FavoriteText = SiestaTextPrimary
private val FavoriteMuted = SiestaNavUnselected
private val FavoriteBorder = SiestaNavOutline

@Composable
fun FavoriteScreen(
    dishes: List<Dish>,
    onBack: () -> Unit,
    removeFavorites: (Int) -> Unit,
    onGoMenu: () -> Unit,
    onAddToCart: ((Int) -> Unit)? = null,
    onOpenDish: ((Int) -> Unit)? = null,
    badgeCartCount: Int = 0,
    onGoCart: (() -> Unit)? = null
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

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
    ) {

        ScreenHeader(
            title = texts.getString(R.string.ui_favorites_38),
            onBack = onBack,
            compact = true,
            centerTitle = false,
            titleFontSize = 24.sp,
            trailing = if (onGoCart != null) {
                {
                    SiestaCartButton(badgeCartCount, onGoCart)
                }
            } else {
                null
            },
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ЗАГОЛОВОК
            if (dishes.isNotEmpty()) item(key = "favorites_header") {

                Text(
                    text = texts.getString(R.string.ui_your_favorites_264),
                    fontFamily = SiestaManrope,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = FavoriteText
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    text = texts.getQuantityString(R.plurals.favorite_count, dishes.size, dishes.size),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    color = FavoriteMuted
                )
            }

            if (dishes.isEmpty()) {

                item(key = "empty_favorites") {
                    EmptyStateContent(
                        title = texts.getString(R.string.ui_save_your_favorites_265),
                        message = texts.getString(R.string.ui_tap_the_heart_on_a_dish_to_find_it_here_266),
                        buttonText = texts.getString(R.string.ui_choose_a_dish_267),
                        onButtonClick = onGoMenu
                    )
                }

            } else {

                items(
                    items = dishes,
                    key = { it.id }
                ) { dish ->

                    SiestaDishCard(
                        dish = dish,
                        onRemove = {
                            removeFavorites(dish.id)
                        },
                        onAddToCart = onAddToCart?.let { callback ->
                            { callback(dish.id) }
                        },
                        onOpenDish = onOpenDish?.let { callback ->
                            { callback(dish.id) }
                        }
                    )
                }

                item(key = "menu_button") {
                    FavoriteMenuButton(
                        onClick = onGoMenu
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteMenuButton(
    onClick: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaCardTop,
                        SiestaCardMiddle
                    )
                )
            )
            .border(
                width = 1.dp,
                color = FavoriteBorder,
                shape = shape
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = texts.getString(R.string.ui_view_menu_268),
            fontFamily = SiestaManrope,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FavoriteText,
            textAlign = TextAlign.Center
        )
    }
}

