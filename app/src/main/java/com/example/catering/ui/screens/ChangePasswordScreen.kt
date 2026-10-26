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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaManrope

private val PasswordGold = SiestaAmber
private val PasswordWhite = SiestaTextPrimary
private val PasswordMuted = SiestaNavUnselected
private val PasswordBorder = SiestaNavOutline
private val PasswordError = SiestaError

@Composable
fun ChangePasswordScreen(
    user: User?,
    onBack: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    // Пароли храним только в памяти Compose.
    // rememberSaveable НЕ используем для секретных данных.
    var currentPassword by remember(user?.id) {
        mutableStateOf("")
    }

    var newPassword by remember(user?.id) {
        mutableStateOf("")
    }

    var confirmPassword by remember(user?.id) {
        mutableStateOf("")
    }

    val passwordsMatch =
        newPassword == confirmPassword

    val passwordIsValid =
        newPassword.length in 6..128

    val currentPasswordIsValid =
        currentPassword.length in 6..128

    val passwordIsDifferent =
        currentPassword != newPassword

    val canSubmit =
        user != null &&
                currentPasswordIsValid &&
                passwordIsValid &&
                confirmPassword.isNotEmpty() &&
                passwordsMatch &&
                passwordIsDifferent &&
                !isSubmitting

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
            title = texts.getString(R.string.ui_change_password_191),
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
                text = texts.getString(R.string.ui_new_password_192),
                fontFamily = SiestaManrope,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PasswordWhite
            )

            Spacer(Modifier.height(9.dp))

            Text(
                text = texts.getString(R.string.ui_update_your_password_to_protect_your_account_193),
                fontFamily = SiestaManrope,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = PasswordMuted
            )

            Spacer(Modifier.height(28.dp))

            if (user == null) {

                Text(
                    text = texts.getString(R.string.ui_sign_in_to_change_your_password_194),
                    color = PasswordMuted,
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp
                )

            } else {

                PasswordField(
                    label = texts.getString(R.string.ui_current_password_195),
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                    },
                    enabled = !isSubmitting
                )

                Spacer(Modifier.height(23.dp))

                PasswordField(
                    label = texts.getString(R.string.ui_new_password_192),
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                    },
                    enabled = !isSubmitting
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = texts.getString(R.string.ui_between_6_and_128_characters_we_recommend_at_least_12_196),
                    fontFamily = SiestaManrope,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    color = PasswordMuted
                )

                Spacer(Modifier.height(23.dp))

                PasswordField(
                    label = texts.getString(R.string.ui_confirm_new_password_197),
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    enabled = !isSubmitting,
                    isLastField = true
                )

                if (
                    confirmPassword.isNotEmpty() &&
                    !passwordsMatch
                ) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = texts.getString(R.string.ui_passwords_do_not_match_28),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        color = PasswordError
                    )
                }

                if (
                    newPassword.isNotEmpty() &&
                    newPassword == currentPassword
                ) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = texts.getString(R.string.ui_your_new_password_must_be_different_from_the_current_one_198),
                        fontFamily = SiestaManrope,
                        fontSize = 12.sp,
                        color = PasswordError
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = texts.uiMessage(errorMessage),
                        fontFamily = SiestaManrope,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = PasswordError
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = texts.getString(R.string.ui_you_will_need_to_sign_in_again_after_changing_your_passwor_199),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = PasswordMuted
                )

                Spacer(Modifier.height(20.dp))
            }
        }

        if (user != null) {

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

                PasswordSaveButton(
                    text = if (isSubmitting) {
                        texts.getString(R.string.ui_saving_164)
                    } else {
                        texts.getString(R.string.ui_save_new_password_200)
                    },
                    enabled = canSubmit,
                    onClick = {
                        onChangePassword(
                            currentPassword,
                            newPassword
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    isLastField: Boolean = false
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var isVisible by remember {
        mutableStateOf(false)
    }

    Column {

        Text(
            text = label,
            fontFamily = SiestaManrope,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PasswordMuted
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isLastField) {
                    ImeAction.Done
                } else {
                    ImeAction.Next
                }
            ),
            trailingIcon = {

                IconButton(
                    onClick = {
                        isVisible = !isVisible
                    },
                    enabled = enabled
                ) {

                    Icon(
                        imageVector = if (isVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (isVisible) {
                            texts.getString(R.string.ui_hide_password_201)
                        } else {
                            texts.getString(R.string.ui_show_password_202)
                        },
                        tint = PasswordMuted
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SiestaManrope,
                fontSize = 14.sp,
                color = PasswordWhite
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SiestaSearchMiddle,
                unfocusedContainerColor = SiestaSearchMiddle,
                disabledContainerColor = SiestaSearchMiddle,
                focusedTextColor = PasswordWhite,
                unfocusedTextColor = PasswordWhite,
                focusedBorderColor = PasswordGold,
                unfocusedBorderColor = PasswordBorder,
                cursorColor = PasswordGold
            )
        )
    }
}

@Composable
private fun PasswordSaveButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    if (enabled) {
                        listOf(
                            SiestaCtaGoldLight,
                            PasswordGold,
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
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                SiestaBackgroundDark
            } else {
                PasswordMuted
            }
        )
    }
}
