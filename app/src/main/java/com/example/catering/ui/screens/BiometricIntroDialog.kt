
package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.catering.ui.theme.SiestaManrope

private val Gold = SiestaAmber
private val White = SiestaTextPrimary
private val Muted = SiestaNavUnselected

@Composable
fun BiometricIntroDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Dialog(
        onDismissRequest = onDismiss
    ) {
        val shape = RoundedCornerShape(28.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SiestaCardTop,
                            SiestaCardMiddle,
                            SiestaBgMiddle
                        )
                    )
                )
                .border(
                    1.dp,
                    SiestaNavOutline,
                    shape
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = texts.getString(R.string.ui_siesta_security_166),
                fontFamily = SiestaManrope,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
                color = Gold
            )

            Spacer(Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SiestaCardTop)
                    .border(
                        1.dp,
                        SiestaNavOutline,
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = texts.getString(R.string.ui_your_account_nis_protected_167),
                fontFamily = SiestaManrope,
                fontSize = 24.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = texts.getString(R.string.ui_one_touch_n_168) +
                        texts.getString(R.string.ui_and_you_are_back_in_siesta_169),
                fontFamily = SiestaManrope,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Muted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SiestaSearchMiddle)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = texts.getString(R.string.ui_android_verifies_your_fingerprint_170) +
                            texts.getString(R.string.ui_siesta_does_not_store_biometric_data_171),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Muted
                )
            }

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = SiestaBackgroundDark
                )
            ) {
                Text(
                    text = texts.getString(R.string.ui_continue_172),
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            TextButton(onClick = onDismiss) {
                Text(
                    text = texts.getString(R.string.ui_not_now_173),
                    fontFamily = SiestaManrope,
                    fontSize = 13.sp,
                    color = Muted
                )
            }
        }
    }
}
