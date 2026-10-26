package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.ui.theme.SiestaManrope

private val UnlockGold = SiestaAmber
private val UnlockWhite = SiestaTextPrimary
private val UnlockMuted = SiestaNavUnselected

@Composable
fun BiometricUnlockScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onUnlockClick: () -> Unit,
    onUsePasswordClick: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaBgTop,
                        SiestaBgMiddle,
                        SiestaBgBottom
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(65.dp))

        // Иконка отпечатка
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(SiestaCardTop)
                .border(
                    width = 1.dp,
                    color = SiestaNavOutline,
                    shape = RoundedCornerShape(30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = UnlockGold,
                modifier = Modifier.size(66.dp)
            )
        }

        Spacer(Modifier.height(30.dp))

        Text(
            text = texts.getString(R.string.ui_welcome_back_174),
            fontFamily = SiestaManrope,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = UnlockWhite,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = texts.getString(R.string.ui_confirm_with_your_fingerprint_n_175) +
                    texts.getString(R.string.ui_to_sign_in_to_your_siesta_account_176),
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = UnlockMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(50.dp))

        // Ошибка проверки
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = texts.uiMessage(errorMessage),
                fontFamily = SiestaManrope,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SiestaError,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
        }

        // Основная кнопка
        Button(
            onClick = onUnlockClick,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = UnlockGold,
                contentColor = SiestaBackgroundDark
            )
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SiestaBackgroundDark,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = texts.getString(R.string.ui_sign_in_with_fingerprint_177),
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        // Альтернативный способ входа
        OutlinedButton(
            onClick = onUsePasswordClick,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                SiestaNavOutline
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = UnlockWhite
            )
        ) {
            Text(
                text = texts.getString(R.string.ui_sign_in_with_password_178),
                fontFamily = SiestaManrope,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = texts.getString(R.string.ui_your_fingerprint_is_stored_by_android_n_179) +
                    texts.getString(R.string.ui_not_by_the_app_180),
            fontFamily = SiestaManrope,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            color = UnlockMuted,
            textAlign = TextAlign.Center
        )
    }
}
