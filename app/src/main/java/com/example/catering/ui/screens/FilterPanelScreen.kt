
package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaSearchBottom
import com.example.catering.ui.theme.SiestaSearchTop

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.PriceRange
import com.example.catering.presentation.menu.SortingOfDishes
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaGold
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary

private val FilterFieldTop = SiestaSearchTop
private val FilterFieldBottom = SiestaSearchBottom
private val FilterTextMuted = SiestaNavUnselected

@Composable
fun FilterPanel(
    onCategoryChange: (String) -> Unit,
    categories: List<String>,
    onPriceRangeChange: (PriceRange) -> Unit,
    priceReduction: () -> Unit,
    selectedPriceRange: PriceRange?,
    selectedCategory: String,
    selectedSort: SortingOfDishes,
    onSortChange: (SortingOfDishes) -> Unit,
    onClose: () -> Unit,
    onClearAllFilters: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var minPriceText by rememberSaveable(
        selectedPriceRange?.minPrice,
        selectedPriceRange?.maxPrice
    ) {
        mutableStateOf(
            selectedPriceRange?.minPrice?.toString().orEmpty()
        )
    }

    var maxPriceText by rememberSaveable(
        selectedPriceRange?.minPrice,
        selectedPriceRange?.maxPrice
    ) {
        mutableStateOf(
            selectedPriceRange?.maxPrice?.toString().orEmpty()
        )
    }

    var priceErrorText by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 22.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            // ЗАГОЛОВОК
            item(key = "intro") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = texts.getString(R.string.ui_your_menu_272),
                        fontFamily = SiestaManrope,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiestaTextPrimary
                    )

                    Text(
                        text = texts.getString(R.string.ui_choose_a_category_and_price_range_273),
                        fontFamily = SiestaManrope,
                        fontSize = 15.sp,
                        color = FilterTextMuted
                    )
                }
            }

            // КАТЕГОРИИ
            item(key = "categories") {
                Column {
                    FilterSectionTitle(
                        text = texts.getString(R.string.ui_categories_274),
                        symbol = "▦"
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterCategoryChip(
                                text = category,
                                selected = category == selectedCategory,
                                onClick = {
                                    onCategoryChange(category)
                                }
                            )
                        }
                    }

                }
            }

            // ЦЕНА
            item(key = "price") {
                Column {
                    FilterSectionTitle(
                        text = texts.getString(R.string.ui_price_range_275),
                        symbol = "₽"
                    )

                    Spacer(Modifier.height(14.dp))

                    FilterPriceField(
                        label = texts.getString(R.string.ui_from_276),
                        value = minPriceText,
                        placeholder = "0",
                        onValueChange = {
                            minPriceText = it.filter(Char::isDigit)
                            priceErrorText = null
                        }
                    )

                    Spacer(Modifier.height(18.dp))

                    FilterPriceField(
                        label = texts.getString(R.string.ui_to_277),
                        value = maxPriceText,
                        placeholder = "1000",
                        onValueChange = {
                            maxPriceText = it.filter(Char::isDigit)
                            priceErrorText = null
                        }
                    )

                    if (priceErrorText != null) {
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = priceErrorText.orEmpty(),
                            color = SiestaError,
                            fontFamily = SiestaManrope,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // СОРТИРОВКА
            item(key = "sorting") {
                Column {
                    FilterSectionTitle(
                        text = texts.getString(R.string.ui_sort_by_278),
                        symbol = "↕"
                    )

                    Spacer(Modifier.height(12.dp))

                    SortingOfDishes.entries.forEachIndexed { index, option ->

                        val label = when (option) {
                            SortingOfDishes.DEFAULT ->
                                texts.getString(R.string.ui_default_279)

                            SortingOfDishes.PRICE_ASC ->
                                texts.getString(R.string.ui_lowest_price_first_280)

                            SortingOfDishes.PRICE_DESC ->
                                texts.getString(R.string.ui_highest_price_first_281)
                        }

                        FilterSortingRow(
                            text = label,
                            selected = selectedSort == option,
                            onClick = {
                                onSortChange(option)
                            }
                        )

                        if (index != SortingOfDishes.entries.lastIndex) {
                            Spacer(Modifier.height(9.dp))
                        }
                    }
                }
            }

            // НЕБОЛЬШАЯ ССЫЛКА СБРОСА
            item(key = "reset") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = texts.getString(R.string.ui_reset_filters_282),
                        modifier = Modifier
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    minPriceText = ""
                                    maxPriceText = ""
                                    priceErrorText = null
                                    onClearAllFilters()
                                }
                            )
                            .padding(
                                horizontal = 4.dp,
                                vertical = 12.dp
                            ),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        color = SiestaNavUnselected
                    )
                }
            }
        }

        // ГЛАВНАЯ КНОПКА ЗАКРЕПЛЕНА СНИЗУ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SiestaCardMiddle)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 14.dp
                )
        ) {
            FilterShowDishesButton(
                onClick = {
                    val min = minPriceText.toIntOrNull()
                    val max = maxPriceText.toIntOrNull()

                    when {
                        minPriceText.isBlank() &&
                                maxPriceText.isBlank() -> {

                            if (selectedPriceRange != null) {
                                priceReduction()
                            }

                            priceErrorText = null
                            onClose()
                        }

                        min == null || max == null -> {
                            priceErrorText =
                                texts.getString(R.string.ui_enter_both_ends_of_the_price_range_283)
                        }

                        min > max -> {
                            priceErrorText =
                                texts.getString(R.string.ui_the_minimum_price_cannot_exceed_the_maximum_284)
                        }

                        else -> {
                            priceErrorText = null

                            onPriceRangeChange(
                                PriceRange(
                                    minPrice = min,
                                    maxPrice = max
                                )
                            )

                            onClose()
                        }
                    }
                }
            )
        }
    }
}
@Composable
private fun FilterSectionTitle(
    text: String,
    symbol: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = symbol,
            fontSize = if (symbol == "↕") 28.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = SiestaAmber
        )

        Text(
            text = text,
            fontFamily = SiestaManrope,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SiestaTextPrimary
        )
    }
}


@Composable
private fun FilterCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            SiestaAmber.copy(alpha = 0.18f),
                            SiestaAmber.copy(alpha = 0.08f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            SiestaSearchTop,
                            SiestaCardMiddle
                        )
                    )
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
            .heightIn(min = 48.dp)
            .padding(
                horizontal = 19.dp,
                vertical = 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
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

@Composable
private fun FilterPriceField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontFamily = SiestaManrope,
            fontSize = 12.sp,
            color = FilterTextMuted
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = SiestaManrope,
                    fontSize = 13.sp,
                    color = SiestaNavUnselected
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SiestaManrope,
                fontSize = 14.sp
            ),
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SiestaTextPrimary,
                unfocusedTextColor = SiestaTextPrimary,
                focusedContainerColor = FilterFieldTop,
                unfocusedContainerColor = FilterFieldBottom,
                focusedBorderColor = SiestaAmber,
                unfocusedBorderColor = SiestaNavOutline,
                cursorColor = SiestaAmber
            )
        )
    }
}

@Composable
private fun FilterSortingRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        FilterFieldTop,
                        FilterFieldBottom
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SiestaNavOutline,
                shape = shape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontSize = 13.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            color = SiestaTextPrimary
        )

        Text(
            text = if (selected) "✓" else "›",
            fontSize = 20.sp,
            color = if (selected) {
                SiestaTextPrimary
            } else {
                SiestaNavUnselected
            }
        )
    }
}

@Composable
private fun FilterShowDishesButton(
    onClick: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SiestaCtaGoldLight,
                        SiestaAmber
                    )
                )
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                start = 19.dp,
                end = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texts.getString(R.string.ui_show_dishes_285),
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SiestaBackgroundDark
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SiestaCtaGoldDark)
                .border(
                    width = 1.dp,
                    color = SiestaCtaGoldLight,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "→",
                fontSize = 22.sp,
                color = SiestaTextPrimary
            )
        }
    }
}
