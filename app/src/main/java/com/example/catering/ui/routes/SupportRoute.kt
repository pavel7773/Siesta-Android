package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import com.example.catering.ui.screens.SupportScreen

@Composable
fun SupportRoute(
    onBack: () -> Unit,
    onCallSupportClick: () -> Unit
) {
    SupportScreen(
        onBack = onBack,
        onCallSupportClick = onCallSupportClick
    )
}