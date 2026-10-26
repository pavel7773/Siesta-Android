
package com.example.catering.ui.components.common

import com.example.catering.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaNavOutline

@Composable
fun SiestaCartButton(
    badgeCartCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    BadgedBox(
        modifier = modifier,
        badge = {
            if (badgeCartCount > 0) {
                Badge(
                    containerColor = SiestaAmber,
                    contentColor = Color(0xFF10191E)
                ) {
                    Text(if (badgeCartCount > 99) "99+" else badgeCartCount.toString())
                }
            }
        }
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape)
                .background(Color(0xFF182A32)).border(1.dp, SiestaNavOutline, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ShoppingCart,
                    contentDescription = texts.getString(R.string.ui_open_cart_items_118, badgeCartCount),
                    tint = SiestaAmber, modifier = Modifier.size(23.dp))
            }
        }
    }
}
