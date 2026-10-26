
package com.example.catering.ui.components.common

import com.example.catering.R

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.catering.ui.theme.SiestaAmber

@Composable
fun SiestaAddToCartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Box(
        modifier = modifier
            .size(width = 56.dp, height = 48.dp)
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 34.dp)
                .border(
                    width = 1.dp,
                    color = SiestaAmber,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = texts.getString(R.string.ui_add_to_cart_117),
                tint = SiestaAmber,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
