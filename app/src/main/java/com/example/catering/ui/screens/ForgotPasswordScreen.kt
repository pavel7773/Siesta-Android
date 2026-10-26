package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaSearchTop
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaManrope

private val RecoveryGold = SiestaAmber
private val RecoveryWhite = SiestaTextPrimary
private val RecoveryMuted = SiestaNavUnselected
private val RecoveryBorder = SiestaNavOutline
private val RecoveryError = SiestaError

private val RecoveryEmailRegex =
    Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onRequestCode: (String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var email by rememberSaveable {
        mutableStateOf("")
    }

    val normalizedEmail = email.trim()

    val isEmailValid =
        normalizedEmail.length in 5..254 &&
                RecoveryEmailRegex.matches(normalizedEmail)

    val canSubmit = isEmailValid && !isSubmitting

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
            .imePadding()
    ) {

        ScreenHeader(
            title = texts.getString(R.string.ui_reset_password_286),
            onBack = onBack,
            compact = true,
            titleFontSize = 19.sp,
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 8.dp
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(25.dp))

            Text(
                text = texts.getString(R.string.ui_recover_your_account_287),
                fontFamily = SiestaManrope,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = RecoveryWhite
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = texts.getString(R.string.ui_enter_the_email_address_288) +
                        texts.getString(R.string.ui_linked_to_your_siesta_account_289),
                fontFamily = SiestaManrope,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = RecoveryMuted
            )

            Spacer(Modifier.height(30.dp))

            Text(
                text = texts.getString(R.string.ui_email_address_249),
                fontFamily = SiestaManrope,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = RecoveryMuted
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "example@mail.ru",
                        color = RecoveryMuted,
                        fontFamily = SiestaManrope
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    color = RecoveryWhite
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SiestaSearchMiddle,
                    unfocusedContainerColor = SiestaSearchMiddle,
                    disabledContainerColor = SiestaSearchMiddle,
                    focusedTextColor = RecoveryWhite,
                    unfocusedTextColor = RecoveryWhite,
                    focusedBorderColor = RecoveryGold,
                    unfocusedBorderColor = RecoveryBorder,
                    cursorColor = RecoveryGold
                )
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = texts.uiMessage(errorMessage),
                    fontFamily = SiestaManrope,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = RecoveryError
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = texts.getString(R.string.ui_if_the_address_is_verified_290) +
                        texts.getString(R.string.ui_we_will_send_you_a_six_digit_code_291) +
                        texts.getString(R.string.ui_the_code_is_valid_for_10_minutes_292),
                fontFamily = SiestaManrope,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                color = RecoveryMuted
            )

            Spacer(Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SiestaCardMiddle)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 16.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            if (canSubmit) {
                                listOf(
                                    SiestaCtaGoldLight,
                                    RecoveryGold,
                                    SiestaCtaGoldDark
                                )
                            } else {
                                listOf(
                                    SiestaSearchTop,
                                    SiestaSearchMiddle
                                )
                            }
                        )
                    )
                    .clickable(
                        enabled = canSubmit,
                        role = Role.Button,
                        onClick = {
                            onRequestCode(normalizedEmail)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSubmitting) {
                        texts.getString(R.string.ui_sending_293)
                    } else {
                        texts.getString(R.string.ui_send_code_294)
                    },
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSubmit) {
                        SiestaBackgroundDark
                    } else {
                        RecoveryMuted
                    }
                )
            }
        }
    }
}
