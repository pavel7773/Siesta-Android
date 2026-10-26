package com.example.catering.ui.components.common

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.catering.ui.components.state.EmptyStateContent
import com.example.catering.ui.theme.SiestaManrope


private val FavoriteGold = SiestaAmber
private val FavoriteText = SiestaTextPrimary
private val FavoriteMuted = SiestaNavUnselected
private val FavoriteBorder = SiestaNavOutline
@Composable
fun SiestaDishCard(
    dish: Dish,
    onRemove: () -> Unit,
    isFavorite: Boolean = true,
    onAddToCart: (() -> Unit)?,
    onOpenDish: (() -> Unit)?
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val context = LocalContext.current

    val imageModel = remember(dish.imageUrl, context) {
        if (dish.imageUrl.startsWith("siesta_")) {
            context.resources.getIdentifier(
                dish.imageUrl,
                "drawable",
                context.packageName
            ).takeIf { it != 0 }
        } else {
            dish.imageUrl.takeIf { it.isNotBlank() }
        }
    }

    val shape = RoundedCornerShape(19.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaSearchMiddle,
                        SiestaBgBottom
                    )
                )
            )
            .border(
                width = 1.dp,
                color = FavoriteGold,
                shape = shape
            )
    ) {

        // ФОТО ВВЕРХУ КАРТОЧКИ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
        ) {

            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = dish.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        enabled = onOpenDish != null,
                        onClick = { onOpenDish?.invoke() }
                    ),
                loading = {
                    FavoriteImagePlaceholder()
                },
                error = {
                    FavoriteImagePlaceholder()
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )

            // СЕРДЕЧКО НАД ФОТО
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(9.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SiestaSearchBottom.copy(alpha = 230 / 255f))
                    .border(
                        width = 1.dp,
                        color = SiestaNavUnselected,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) texts.getString(R.string.ui_remove_from_favorites_119) else texts.getString(R.string.ui_add_to_favorites_120),
                    tint = if (isFavorite) FavoriteGold else FavoriteMuted,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        // ДАННЫЕ БЛЮДА
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 12.dp,
                    bottom = 13.dp
                )
        ) {

            Text(
                text = dish.name,
                fontFamily = SiestaManrope,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = FavoriteText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(
                    enabled = onOpenDish != null,
                    onClick = { onOpenDish?.invoke() }
                )
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = dish.info,
                fontFamily = SiestaManrope,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = FavoriteMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(13.dp))

            // ЦЕНА И КНОПКА
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "${dish.price} ₽",
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    fontFamily = SiestaManrope,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FavoriteText
                )

                Box(
                    modifier = Modifier
                        .widthIn(min = 112.dp)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(50))
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
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(
                            enabled = onAddToCart != null,
                            role = Role.Button,
                            onClick = { onAddToCart?.invoke() }
                        )
                        .padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = texts.getString(R.string.ui_add_to_cart_121),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (onAddToCart != null) {
                            FavoriteText
                        } else {
                            FavoriteMuted
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteImagePlaceholder() {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SiestaCardTop),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texts.getString(R.string.ui_siesta_122),
            color = FavoriteGold,
            fontSize = 13.sp
        )
    }
}
