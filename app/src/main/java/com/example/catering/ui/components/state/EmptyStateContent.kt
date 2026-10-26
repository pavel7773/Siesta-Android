package com.example.catering.ui.components.state

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.*

@Composable
fun EmptyStateContent(title: String, message: String, modifier: Modifier = Modifier,
                      buttonText: String? = null, onButtonClick: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SiestaCard {
            Icon(Icons.Outlined.ShoppingBag, null, tint = SiestaAmber, modifier = Modifier.size(28.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = SiestaTextPrimary)
            SiestaBody(message)
        }
        if (buttonText != null && onButtonClick != null) SiestaButton(buttonText, onButtonClick)
    }
}
