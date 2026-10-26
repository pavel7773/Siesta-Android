package com.example.catering.ui.screens

import com.example.catering.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.catering.domain.model.Dish
import com.example.catering.ui.components.common.*
import com.example.catering.ui.components.state.EmptyStateContent

@Composable
fun RecentlyViewed(onBack: () -> Unit, clear: () -> Unit, dishes: List<Dish>,
    onGoMenu: () -> Unit, onOpenDish: (Int) -> Unit, onAddToCart: (Int) -> Unit,
    onGoCart: (() -> Unit)? = null, badgeCartCount: Int = 0,
    favoriteIds: List<Int>, onToggleFavorite: (Int, Boolean) -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Column(Modifier.fillMaxSize().background(SiestaScreenBrush)) {
        ScreenHeader(texts.getString(R.string.ui_recently_viewed_306), onBack, compact = true,
            trailing = onGoCart?.let { { SiestaCartButton(badgeCartCount, it) } },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (dishes.isEmpty()) item {
                EmptyStateContent(texts.getString(R.string.ui_your_next_choice_awaits_429), texts.getString(R.string.ui_dishes_you_open_will_appear_here_430),
                    buttonText = texts.getString(R.string.ui_view_menu_268), onButtonClick = onGoMenu)
            } else {
                item { SiestaHeading(texts.getString(R.string.ui_pick_up_where_you_left_off_431)); Spacer(Modifier.height(8.dp)); SiestaBody(texts.getString(R.string.ui_dishes_you_have_viewed_432)) }
                items(dishes, key = { it.id }) { dish ->
                    SiestaDishCard(dish, isFavorite = dish.id in favoriteIds,
                        onRemove = { onToggleFavorite(dish.id, dish.id in favoriteIds) },
                        onAddToCart = { onAddToCart(dish.id) }, onOpenDish = { onOpenDish(dish.id) })
                }
                item { SiestaButton(texts.getString(R.string.ui_clear_history_433), clear, secondary = true) }
            }
        }
    }
}
