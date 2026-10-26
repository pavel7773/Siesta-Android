package com.example.catering.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.catering.R

// Set of Material typography styles to start with

val SiestaManrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semi_bold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)
private val DefaultTypography = Typography()
val Typography = with(DefaultTypography) {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = SiestaManrope),
        displayMedium = displayMedium.copy(fontFamily = SiestaManrope),
        displaySmall = displaySmall.copy(fontFamily = SiestaManrope),
        headlineLarge = headlineLarge.copy(fontFamily = SiestaManrope),
        headlineMedium = headlineMedium.copy(fontFamily = SiestaManrope),
        headlineSmall = headlineSmall.copy(fontFamily = SiestaManrope),
        titleLarge = titleLarge.copy(fontFamily = SiestaManrope),
        titleMedium = titleMedium.copy(fontFamily = SiestaManrope),
        titleSmall = titleSmall.copy(fontFamily = SiestaManrope),
        bodyLarge = bodyLarge.copy(fontFamily = SiestaManrope, letterSpacing = 0.sp),
        bodyMedium = bodyMedium.copy(fontFamily = SiestaManrope),
        bodySmall = bodySmall.copy(fontFamily = SiestaManrope),
        labelLarge = labelLarge.copy(fontFamily = SiestaManrope),
        labelMedium = labelMedium.copy(fontFamily = SiestaManrope),
        labelSmall = labelSmall.copy(fontFamily = SiestaManrope)
    )
}
