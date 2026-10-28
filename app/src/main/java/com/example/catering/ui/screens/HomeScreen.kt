package com.example.catering.ui.screens


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.catering.R
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.SiestaAddToCartButton
import com.example.catering.ui.components.common.SiestaCartButton
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchBottom
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaSearchTop
import com.example.catering.ui.theme.SiestaTextPrimary

private val SiestaCormorant = FontFamily(
    Font(
        R.font.cormorant_garamond_medium,
        weight = FontWeight.Medium
    ),
    Font(
        R.font.cormorant_garamond_semibold,
        weight = FontWeight.SemiBold
    )
)

private val SiestaManrope = FontFamily(
    Font(
        R.font.manrope_regular,
        weight = FontWeight.Normal
    ),
    Font(
        R.font.manrope_medium,
        weight = FontWeight.Medium
    ),
    Font(
        R.font.manrope_semi_bold,
        weight = FontWeight.SemiBold
    ),
    Font(
        R.font.manrope_bold,
        weight = FontWeight.Bold
    )
)

@Composable
fun HomeScreen(
    onGoToCart: () -> Unit,
    badgeCartCount: Int,
    onExitApp: () -> Unit,
    onGoViewedDish: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    categoryDishes: List<Dish>,
    onCategoryClick: (String) -> Unit,
    recentlyViewedDishes: List<Dish> = emptyList(),
    onOpenRecentlyViewedDish: (Int) -> Unit = {},
    onAddRecentlyViewedToCart: (Dish) -> Unit = {}
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var showMoreCategories by rememberSaveable {
        mutableStateOf(false)
    }

    var showDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val submitSearch = { focusManager.clearFocus(); onSearch() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                brush = Brush.verticalGradient(
                    0.0f to SiestaBgTop,
                    0.46f to SiestaBgMiddle,
                    1.0f to SiestaBgBottom
                )
            )
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = texts.getString(R.string.ui_pickup_36_chernikova_st_295),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = texts.getString(R.string.ui_siesta_nearby_296),
                        fontFamily = SiestaManrope,
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        color = SiestaNavUnselected
                    )
                }
                SiestaCartButton(
                    badgeCartCount = badgeCartCount,
                    onClick = onGoToCart,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Фотография пасты с плавным растворением
                Image(
                    painter = painterResource(R.drawable.siesta_hero_pasta),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            // Растворение слева и справа
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.25f to Color.White,
                                    0.75f to Color.White,
                                    1.0f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )

                            // Растворение сверху и снизу
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.15f to Color.White,
                                    0.65f to Color.White,
                                    1.0f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                )

                // Логотип и текст поверх фотографии
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.siesta_logo),
                                contentDescription = texts.getString(R.string.ui_siesta_logo_297),
                                modifier = Modifier
                                    .width(78.dp)
                                    .height(76.dp),
                                contentScale = ContentScale.Fit
                            )

                            Column {
                                Text(
                                    text = texts.getString(R.string.ui_siesta_122),
                                    fontFamily = SiestaCormorant,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = texts.getString(R.string.ui_kitchen_delivery_298),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = texts.getString(R.string.ui_mediterranean_nflavor_closer_299),
                        fontFamily = SiestaCormorant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp,
                        lineHeight = 29.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = texts.getString(R.string.ui_fresh_ingredients_nfavorite_dishes_300),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(26.dp))
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    SiestaSearchTop.copy(alpha = 0.72f),
                                    SiestaSearchMiddle.copy(alpha = 0.65f),
                                    SiestaSearchBottom.copy(alpha = 0.72f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = SiestaNavOutline,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 52.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = texts.getString(R.string.ui_search_dishes_301),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = submitSearch)
                                .padding(9.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription =
                                        texts.getString(R.string.ui_search_your_favorite_dishes_302)
                                },
                            textStyle = TextStyle(
                                fontFamily = SiestaManrope,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush = SolidColor(SiestaAmber),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = texts.getString(R.string.ui_search_your_favorite_dishes_302),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = SiestaManrope,
                                            fontSize = 14.sp,
                                            color = SiestaNavUnselected
                                        )
                                    }

                                    innerTextField()
                                }
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 1.dp)
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { focusManager.clearFocus(); onOpenFilters() }
                            .semantics {
                                contentDescription =
                                    texts.getString(R.string.ui_filters_and_categories_303)
                            }
                            .background(
                                color = Color(0xFF24343E).copy(alpha = 0.85f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.size(20.dp)
                        ) {
                            val iconColor = Color(0xFFF5F2EA)
                            val stroke = 1.8.dp.toPx()

                            for (i in 0..2) {
                                val y = size.height * (0.2f + i * 0.3f)
                                val circleX = size.width *
                                        (if (i == 1) 0.35f else 0.7f)

                                drawLine(
                                    color = iconColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round
                                )

                                drawCircle(
                                    color = iconColor,
                                    radius = 2.6.dp.toPx(),
                                    center = Offset(circleX, y)
                                )
                            }
                        }
                    }
                }

            }
        }


        // Categories come from the real menu, not from decorative hard-coded labels.
        item {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                categoryDishes
                    .take(if (showMoreCategories) categoryDishes.size else 4)
                    .forEach { dish ->
                    item(key = dish.category) {
                        val context = LocalContext.current
                        val model = remember(dish.imageUrl, context) {
                            if (dish.imageUrl.startsWith("siesta_")) context.resources.getIdentifier(
                                dish.imageUrl, "drawable", context.packageName
                            ).takeIf { it != 0 }
                            else dish.imageUrl.takeIf { it.isNotBlank() }
                        }
                        Column(
                            Modifier
                                .width(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button) {
                                    focusManager.clearFocus(); onCategoryClick(
                                    dish.category
                                )
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            SubcomposeAsyncImage(
                                model = model, contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, SiestaNavOutline, CircleShape),
                                loading = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(SiestaSearchMiddle)
                                    )
                                },
                                error = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(SiestaSearchMiddle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(dish.category.take(1), color = SiestaAmber)
                                    }
                                })
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (dish.category == "Морепродукты") texts.getString(R.string.ui_seafood_304) else dish.category,
                                fontFamily = SiestaManrope,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (!showMoreCategories && categoryDishes.size > 4) {
                    item(key = "all-categories") {
                        Column(
                            Modifier
                                .width(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button) {
                                    focusManager.clearFocus()
                                    showMoreCategories = true
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(60.dp)
                                    .background(SiestaSearchMiddle, CircleShape)
                                    .border(1.dp, SiestaNavOutline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "•••",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                texts.getString(R.string.ui_more_305),
                                fontFamily = SiestaManrope,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
        if (recentlyViewedDishes.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = texts.getString(R.string.ui_recently_viewed_306),
                            modifier = Modifier.weight(1f),
                            fontFamily = SiestaCormorant,
                            fontSize = 25.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        TextButton(onClick = onGoViewedDish) {
                            Text(
                                text = texts.getString(R.string.ui_all_307),
                                color = SiestaAmber,
                                fontFamily = SiestaManrope
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val columns = if (maxWidth < 360.dp) 1 else 2
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recentlyViewedDishes.take(2).chunked(columns).forEach { rowDishes ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowDishes.forEach { dish ->
                                        val context = LocalContext.current

                                        val imageModel: Any? = remember(
                                            dish.imageUrl,
                                            context
                                        ) {
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

                                        Card(
                                            onClick = {
                                                onOpenRecentlyViewedDish(dish.id)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF1A282F)
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = SiestaAmber.copy(alpha = 0.55f)
                                            )
                                        ) {
                                            Column {
                                                SubcomposeAsyncImage(
                                                    model = imageModel,
                                                    contentDescription = dish.name,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(112.dp),
                                                    contentScale = ContentScale.Crop,
                                                    error = {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                texts.getString(R.string.ui_no_photo_308),
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                )

                                                Column(
                                                    modifier = Modifier.padding(10.dp)
                                                ) {
                                                    Text(
                                                        text = dish.name,
                                                        fontFamily = SiestaManrope,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        maxLines = 2,
                                                        minLines = 2
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${dish.price} ₽",
                                                            fontFamily = SiestaManrope,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = SiestaAmber
                                                        )

                                                        SiestaAddToCartButton(
                                                            onClick = {
                                                                onAddRecentlyViewedToCart(
                                                                    dish
                                                                )
                                                            },
                                                            modifier = Modifier.offset(
                                                                x = 4.dp,
                                                                y = 5.dp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    BackHandler(enabled = !showDialog) {
        showDialog = true
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = texts.getString(R.string.ui_exit_309)) },
            text = { Text(texts.getString(R.string.ui_are_you_sure_you_want_to_leave_the_app_310)) },
            confirmButton = {
                TextButton(onClick = {
                    onExitApp()
                }
                ) {
                    Text(texts.getString(R.string.ui_yes_311))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(texts.getString(R.string.ui_no_312))
                }
            }
        )
    }
}


@Composable
fun CartSection(
    cartItems: List<CartItem>,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    favoriteDishIds: List<Int>,
    onToggleFavorite: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    if (cartItems.isEmpty()) {
        Text(
            text = texts.getString(R.string.ui_your_cart_is_empty_313),
            style = MaterialTheme.typography.titleMedium
        )
    } else {
        Column(
            modifier = modifier.padding(2.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems) { cartItem ->

                    val isFavorite =
                        cartItem.dish.id in favoriteDishIds

                    CartItemRow(
                        cartItem = cartItem,
                        onIncrease = {
                            onIncrease(cartItem)
                        },
                        onDecrease = {
                            onDecrease(cartItem)
                        },
                        onRemove = {
                            onRemove(cartItem)
                        },
                        isFavorite = isFavorite,
                        onToggleFavorite = {
                            onToggleFavorite(
                                cartItem.dish.id,
                                isFavorite
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val context = LocalContext.current

    val imageModel: Any? = remember(cartItem.dish.imageUrl, context) {
        if (cartItem.dish.imageUrl.startsWith("siesta_")) {
            context.resources.getIdentifier(
                cartItem.dish.imageUrl,
                "drawable",
                context.packageName
            ).takeIf { it != 0 }
        } else {
            cartItem.dish.imageUrl
        }
    }

    val itemTotalPrice = cartItem.dish.price * cartItem.quantity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF16242C),
                            Color(0xFF0D171D)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    width = 1.dp,
                    color = SiestaAmber.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(8.dp)
        ) {
            Box {
                SubcomposeAsyncImage(
                    model = imageModel,
                    contentDescription = cartItem.dish.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = SiestaAmber,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF10191F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = texts.getString(R.string.ui_photo_unavailable_314),
                                color = SiestaTextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )

                // Сердечко пока визуальное
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xE61A252D))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape
                        )
                ) {

                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (isFavorite) {
                            texts.getString(
                                R.string.ui_remove_from_favorites_315,
                                cartItem.dish.name
                            )
                        } else {
                            texts.getString(R.string.ui_add_to_favorites_316, cartItem.dish.name)
                        },
                        tint = if (isFavorite) {
                            SiestaAmber
                        } else {
                            Color.White
                        },
                        modifier = Modifier.size(20.dp)
                    )

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = cartItem.dish.name,
                style = MaterialTheme.typography.titleMedium,
                color = SiestaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = cartItem.dish.info,
                style = MaterialTheme.typography.bodySmall,
                color = SiestaNavUnselected,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$itemTotalPrice ₽",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp,
                    color = SiestaAmber,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF343B48))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDecrease,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "−",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${cartItem.quantity}",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        TextButton(
                            onClick = onIncrease,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(28.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = texts.getString(
                                R.string.ui_remove_317,
                                cartItem.dish.name
                            ),
                            tint = SiestaNavUnselected,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
