package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaSearchMiddle

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.catering.domain.model.Dish
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.components.common.SiestaCartButton
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaGold
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary
import kotlinx.coroutines.launch

@Composable
fun DishDetailScreen(
    onBack: () -> Unit,
    onGoToCart: () -> Unit,
    badgeCartCount: Int,
    onAddToCart: (Dish) -> Unit,
    onUndoToCart: (Dish) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    dish: Dish?,
    onRetry: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SiestaBgTop,
            SiestaBgMiddle,
            SiestaBgBottom
        )
    )

    Scaffold(
        containerColor = SiestaBgTop,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (dish != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SiestaBgBottom)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SiestaBottomActionButton(
                        text = texts.getString(R.string.ui_add_to_cart_232, dish.price),
                        onClick = {
                            onAddToCart(dish)

                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = texts.getString(R.string.ui_added_to_your_cart_233, dish.name),
                                    actionLabel = texts.getString(R.string.ui_cancel_234),
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoToCart(dish)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(6.dp))

            // Верхняя строка: назад + корзина
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScreenHeader(
                        title = texts.getString(R.string.ui_dish_details_235),
                        compact = true,
                        onBack = onBack,
                        trailing = {
                            SiestaCartButton(
                                badgeCartCount = badgeCartCount,
                                onClick = onGoToCart
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (dish == null) {
                ErrorStateContent(
                    title = texts.getString(R.string.ui_could_not_open_this_dish_236),
                    message = texts.getString(R.string.ui_check_your_internet_connection_nand_try_again_237),
                    buttonText = texts.getString(R.string.ui_retry_141),
                    onButtonClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val context = LocalContext.current

                val imageModel: Any? = remember(dish.imageUrl, context) {
                    if (dish.imageUrl.startsWith("siesta_")) {
                        context.resources.getIdentifier(
                            dish.imageUrl,
                            "drawable",
                            context.packageName
                        ).takeIf { it != 0 }
                    } else {
                        dish.imageUrl
                    }
                }

                // Фото + сердечко
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SiestaCardMiddle)
                        .border(
                            width = 1.dp,
                            color = SiestaNavOutline,
                            shape = RoundedCornerShape(22.dp)
                        )
                ) {
                    SubcomposeAsyncImage(
                        model = imageModel,
                        contentDescription = dish.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SiestaAmber)
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = texts.getString(R.string.ui_dish_photo_unavailable_238),
                                    fontFamily = SiestaManrope,
                                    fontSize = 13.sp,
                                    color = SiestaNavUnselected,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SiestaSearchMiddle.copy(alpha = 204 / 255f))
                            .border(
                                width = 1.dp,
                                color = SiestaNavOutline,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = texts.getString(R.string.ui_favorites_38),
                            tint = if (isFavorite) SiestaAmber else SiestaTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Инфо-блок
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
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
                            color = SiestaNavOutline,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = dish.name,
                        fontFamily = SiestaManrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = SiestaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dish.info,
                        fontFamily = SiestaManrope,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = SiestaNavUnselected
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${dish.price} ₽",
                        fontFamily = SiestaManrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = SiestaAmber
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SiestaCardMiddle)
                            .border(
                                width = 1.dp,
                                color = SiestaAmber.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SiestaCompactNutritionItem(
                                label = texts.getString(R.string.ui_weight_239),
                                value = texts.getString(R.string.ui_g_240, dish.weight),
                                modifier = Modifier.weight(1f)
                            )
                            SiestaCompactNutritionItem(
                                label = texts.getString(R.string.ui_calories_241),
                                value = texts.getString(R.string.ui_kcal_242, dish.calories),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SiestaNavOutline.copy(alpha = 0.55f))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SiestaCompactNutritionItem(
                                label = texts.getString(R.string.ui_protein_243),
                                value = texts.getString(R.string.ui_g_244, dish.proteins),
                                modifier = Modifier.weight(1f)
                            )
                            SiestaCompactNutritionItem(
                                label = texts.getString(R.string.ui_fat_245),
                                value = texts.getString(R.string.ui_g_246, dish.fats),
                                modifier = Modifier.weight(1f)
                            )
                            SiestaCompactNutritionItem(
                                label = texts.getString(R.string.ui_carbs_247),
                                value = texts.getString(R.string.ui_g_248, dish.carbs),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun SiestaCompactNutritionItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            fontFamily = SiestaManrope,
            fontSize = 10.sp,
            color = SiestaNavUnselected
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SiestaTextPrimary
        )
    }
}

@Composable
private fun SiestaBottomActionButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(40.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        SiestaCtaGoldLight, // светло-жёлтый слева
                        SiestaAmber, // золотистый в центре
                        SiestaCtaGoldDark  // глубокое золото справа
                    )
                )

            )
            .border(
                width = 1.dp,
                color = SiestaGold,
                shape = shape
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(start = 18.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
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
                .background(SiestaCtaGoldDark.copy(alpha = 51 / 255f))
                .border(
                    width = 1.dp,
                    color = SiestaTextPrimary.copy(alpha = 85 / 255f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = SiestaTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
