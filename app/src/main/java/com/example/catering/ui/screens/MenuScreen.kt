package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.MenuDataSourceType
import com.example.catering.domain.model.PriceRange
import com.example.catering.presentation.menu.MenuUiState
import com.example.catering.presentation.menu.SortingOfDishes
import com.example.catering.presentation.menu.formatMenuUpdatedAt
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.components.common.SiestaAddToCartButton
import com.example.catering.ui.components.common.SiestaCartButton
import com.example.catering.ui.components.common.SiestaButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.catering.ui.components.state.ErrorStateContent
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaGold
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary

private val MenuSurface = Brush.verticalGradient(
    0f to SiestaCardTop,
    0.48f to SiestaBgTop,
    1f to SiestaBgMiddle
)
private val MenuBody = TextStyle(
    fontFamily = SiestaManrope,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 19.sp,
    color = SiestaNavUnselected
)
private val MenuTitle = MenuBody.copy(
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 23.sp,
    color = SiestaTextPrimary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onOpenDish: (Int) -> Unit,
    onBack: () -> Unit,
    onGoCart: () -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    menuUiState: MenuUiState,
    favoriteDishIds: List<Int>,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<String>,
    onPriceRangeChange: (PriceRange) -> Unit,
    onClearPriceRange: () -> Unit,
    selectedPriceRange: PriceRange?,
    selectedSort: SortingOfDishes,
    onSortChange: (SortingOfDishes) -> Unit,
    onCategoryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onGoSupport: (() -> Unit)? = null,
    openFiltersRequested: Boolean = false,
    onFiltersRequestConsumed: () -> Unit = {},
    badgeCartCount: Int = 0
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var showFilters by rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(openFiltersRequested) {
        if (openFiltersRequested) {
            showFilters = true; onFiltersRequestConsumed()
        }
    }
    val focusManager = LocalFocusManager.current
    val openFilters = {
        focusManager.clearFocus()
        showFilters = true
    }
    BackHandler(enabled = showFilters) { showFilters = false }

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to SiestaBgTop,
                    0.46f to SiestaBgMiddle,
                    1f to SiestaBgBottom
                )
            )
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(SiestaGold.copy(alpha = 0.045f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.2f),
                        radius = size.width * 0.85f
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(SiestaCardTop.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(0f, size.height * 0.6f),
                        radius = size.width
                    )
                )
            }
    ) {

        ScreenHeader(
            title = if (showFilters) texts.getString(R.string.ui_filters_332) else texts.getString(R.string.ui_menu_36),
            onBack = {
                if (showFilters) {
                    showFilters = false
                } else {
                    onBack()
                }
            },
            titleFontSize = 24.sp,
            compact = true,
            centerTitle = true,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            trailing = {
                SiestaCartButton(
                    badgeCartCount = badgeCartCount,
                    onClick = onGoCart
                )
            }
        )


        Box(Modifier.weight(1f)) {
            when (menuUiState) {
                MenuUiState.Loading -> LoadingStateContent(message = texts.getString(R.string.ui_loading_menu_333))
                MenuUiState.Error -> Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ErrorStateContent(
                        texts.getString(R.string.ui_could_not_load_the_menu_334),
                        texts.getString(R.string.ui_check_your_connection_then_try_again_335),
                        buttonText = texts.getString(R.string.ui_retry_141),
                        onButtonClick = onRetry,
                        contentPadding = 0.dp
                    )
                    if (onGoSupport != null) SiestaButton(
                        texts.getString(R.string.ui_contact_the_restaurant_336),
                        onGoSupport,
                        secondary = true
                    )
                }

                is MenuUiState.Success -> {
                    if (showFilters) {
                        // Keep the existing price validation, sorting and filter callbacks intact.
                        FilterPanel(
                            onCategoryChange = onCategoryChange,
                            categories = categories,
                            onPriceRangeChange = onPriceRangeChange,
                            priceReduction = onClearPriceRange,
                            selectedPriceRange = selectedPriceRange,
                            selectedCategory = selectedCategory,
                            selectedSort = selectedSort,
                            onSortChange = onSortChange,
                            onClose = { showFilters = false },
                            onClearAllFilters = {
                                onCategoryChange("Все")
                                onClearPriceRange()
                                onSortChange(SortingOfDishes.DEFAULT)
                            }
                        )
                    } else {
                        val activeFiltersCount = listOf(
                            selectedCategory != "Все",
                            selectedPriceRange != null
                        ).count { it }
                        val filterLabel = if (activeFiltersCount == 0) {
                            texts.getString(R.string.ui_filters_and_sorting_337)
                        } else {
                            texts.getString(R.string.ui_filters_and_sorting_338, activeFiltersCount)
                        }
                        PullToRefreshBox(
                            isRefreshing = menuUiState.isRefreshing,
                            onRefresh = onRefresh,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 14.dp,
                                    end = 14.dp,
                                    top = 8.dp,
                                    bottom = 32.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item(key = "search") {
                                    MenuSearch(
                                        searchQuery,
                                        onSearchQueryChange,
                                        filterLabel,
                                        openFilters
                                    )
                                }
                                item(key = "categories") {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(categories, key = { it }) { category ->
                                            MenuCategory(
                                                category,
                                                category == selectedCategory,
                                                onClick = { onCategoryChange(category) }
                                            )
                                        }
                                    }
                                }
                                if (menuUiState.isRefreshing) {
                                    item(key = "refreshing") { MenuNotice(texts.getString(R.string.ui_refreshing_menu_339)) }
                                }
                                if (menuUiState.source == MenuDataSourceType.CACHE) {
                                    item(key = "cache") {
                                        MenuNotice(
                                            texts.getString(R.string.ui_showing_saved_menu_340),
                                            texts.getString(
                                                R.string.ui_prices_may_change_updated_341,
                                                texts.uiMessage(formatMenuUpdatedAt(menuUiState.lastUpdatedAtMillis))
                                            )
                                        )
                                    }
                                }
                                menuUiState.refreshErrorMessage?.let { message ->
                                    item(key = "refresh-error") {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            MenuNotice(texts.uiMessage(message))
                                            MenuAction(
                                                if (menuUiState.isRefreshing) texts.getString(R.string.ui_refreshing_menu_342) else texts.getString(
                                                    R.string.ui_refresh_menu_343
                                                ),
                                                onRefresh,
                                                Modifier.fillMaxWidth(),
                                                enabled = !menuUiState.isRefreshing
                                            )
                                        }
                                    }
                                }
                                item(key = "heading") {
                                    Text(
                                        when {
                                            searchQuery.isNotBlank() -> texts.getString(R.string.ui_search_results_344)
                                            selectedCategory != "Все" -> selectedCategory
                                            else -> texts.getString(R.string.ui_our_menu_345)
                                        },
                                        style = MenuTitle.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            lineHeight = 36.sp
                                        )
                                    )
                                }
                                if (menuUiState.dishes.isEmpty()) {
                                    item(key = "empty") {
                                        val hasFilters = activeFiltersCount > 0
                                        MenuNotice(
                                            when {
                                                searchQuery.isNotBlank() && hasFilters -> texts.getString(
                                                    R.string.ui_no_matches_for_this_search_and_filters_346
                                                )

                                                searchQuery.isNotBlank() -> texts.getString(R.string.ui_no_matches_for_this_search_347)
                                                hasFilters -> texts.getString(R.string.ui_no_matches_for_these_filters_348)
                                                else -> texts.getString(R.string.ui_the_menu_is_empty_for_now_349)
                                            },
                                            if (searchQuery.isNotBlank() || hasFilters) {
                                                texts.getString(R.string.ui_try_another_name_or_change_the_filters_350)
                                            } else {
                                                texts.getString(R.string.ui_pull_down_to_refresh_the_menu_351)
                                            }
                                        )
                                    }
                                }
                                items(menuUiState.dishes, key = { it.id }) { dish ->
                                    val isFavorite = dish.id in favoriteDishIds
                                    MenuDishCard(
                                        dish = dish,
                                        isFavorite = isFavorite,
                                        onOpen = { onOpenDish(dish.id) },
                                        onFavorite = { onToggleFavorite(dish.id, isFavorite) },
                                        onAddToCart = { onAddToCart(dish.id) }
                                    )
                                }
                                item(key = "filters") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .menuSurface()
                                            .clickable(role = Role.Button, onClick = openFilters)
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                filterLabel,
                                                style = MenuTitle.copy(fontSize = 15.sp)
                                            )
                                            Text(
                                                when (selectedSort) {
                                                    SortingOfDishes.DEFAULT -> texts.getString(R.string.ui_adjust_selection_352)
                                                    SortingOfDishes.PRICE_ASC -> texts.getString(R.string.ui_lowest_price_first_353)
                                                    SortingOfDishes.PRICE_DESC -> texts.getString(R.string.ui_highest_price_first_354)
                                                },
                                                style = MenuBody
                                            )
                                        }
                                        Icon(Icons.Outlined.Tune, null, tint = SiestaTextPrimary)
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

@Composable
private fun MenuSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    filterLabel: String,
    onFilters: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val focusManager = LocalFocusManager.current
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaCardTop.copy(alpha = 0.85f),
                        SiestaBgTop.copy(alpha = 0.56f),
                        SiestaCardMiddle.copy(alpha = 0.8f)
                    )
                )
            )
            .border(1.dp, SiestaNavUnselected.copy(alpha = 0.63f), RoundedCornerShape(24.dp))
            .heightIn(min = 48.dp)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(20.dp), tint = SiestaTextPrimary)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MenuBody.copy(color = SiestaTextPrimary),
            cursorBrush = SolidColor(SiestaAmber),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) Text(
                        texts.getString(R.string.ui_find_a_dish_355),
                        style = MenuBody
                    )
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    Icons.Outlined.Close,
                    texts.getString(R.string.ui_clear_search_356),
                    Modifier.size(18.dp),
                    tint = SiestaNavUnselected
                )
            }
        }
        IconButton(onClick = onFilters, modifier = Modifier.size(48.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MenuSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    filterLabel,
                    Modifier.size(18.dp),
                    tint = SiestaTextPrimary
                )
            }
        }
    }
}

@Composable
private fun MenuCategory(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier
            .widthIn(min = 88.dp)
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(if (isSelected) SolidColor(SiestaAmber.copy(alpha = 0.13f)) else MenuSurface)
            .border(
                1.dp,
                if (isSelected) SiestaAmber.copy(alpha = 0.7f) else SiestaNavOutline,
                shape
            )
            .semantics { selected = isSelected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MenuBody.copy(color = if (isSelected) SiestaGold else SiestaTextPrimary)
        )
    }
}


@Composable
private fun MenuDishCard(
    dish: Dish,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onAddToCart: () -> Unit
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
            dish.imageUrl
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .menuSurface(
                borderColor = SiestaGold.copy(alpha = 0.7f)
            )
            .clickable(
                role = Role.Button,
                onClickLabel = texts.getString(R.string.ui_open_357, dish.name),
                onClick = onOpen
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            // Фотография слева
            Box(
                modifier = Modifier
                    .weight(0.50f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                SubcomposeAsyncImage(
                    model = imageModel,
                    contentDescription = dish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SiestaAmber,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                texts.getString(R.string.ui_no_photo_yet_358),
                                style = MenuBody.copy(fontSize = 11.sp)
                            )
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )

                // Сердечко поверх фотографии
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SiestaCardMiddle.copy(alpha = 230 / 255f))
                            .border(
                                1.dp,
                                SiestaNavOutline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = if (isFavorite) {
                                texts.getString(R.string.ui_remove_from_favorites_119)
                            } else {
                                texts.getString(R.string.ui_add_to_favorites_120)
                            },
                            modifier = Modifier.size(21.dp),
                            tint = if (isFavorite) {
                                SiestaAmber
                            } else {
                                SiestaTextPrimary
                            }
                        )
                    }
                }
            }

            // Информация справа
            Column(
                modifier = Modifier
                    .weight(0.50f)
                    .heightIn(min = 140.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dish.name,
                        style = MenuTitle.copy(
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (dish.info.isNotBlank()) {
                        Text(
                            text = dish.info,
                            style = MenuBody.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${dish.price} ₽",
                        modifier = Modifier.weight(1f),
                        style = MenuTitle.copy(
                            fontSize = 18.sp,
                            color = SiestaAmber
                        ),
                        maxLines = 1
                    )

                    // Пока открывает блюдо.
                    // Рабочую кнопку "+" подключим следующим шагом.
                    SiestaAddToCartButton(
                        onClick = onAddToCart
                    )
                }
            }
        }
    }
}


@Composable
private fun MenuAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .menuSurface(shape = RoundedCornerShape(26.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MenuTitle.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = if (enabled) SiestaTextPrimary else SiestaNavUnselected
            )
        )
    }
}

@Composable
private fun MenuNotice(title: String, message: String? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .menuSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MenuTitle)
        if (message != null) Text(message, style = MenuBody)
    }
}

private fun Modifier.menuSurface(
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color = SiestaNavOutline
): Modifier = this
    .shadow(8.dp, shape, clip = false)
    .clip(shape)
    .background(MenuSurface)
    .border(1.dp, borderColor, shape)
