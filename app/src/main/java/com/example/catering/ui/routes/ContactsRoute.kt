package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import com.example.catering.ui.screens.ContactsScreen

@Composable
fun ContactsRoute(
    onBack: () -> Unit,
    onCallClick: () -> Unit
) {
    ContactsScreen(
        onBack = onBack,
        onCallClick = onCallClick
    )
}