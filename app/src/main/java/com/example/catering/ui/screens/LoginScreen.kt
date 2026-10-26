package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import androidx.compose.foundation.layout.*
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.SiestaAmber

@Composable
fun LoginScreen(onBack: () -> Unit, onGo: (String, String) -> Unit,
                noAccount: () -> Unit, onForgotPassword: () -> Unit,
                errorMessage: String?, isLoading: Boolean = false) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var phone by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    SiestaPage(texts.getString(R.string.ui_sign_in_318), onBack, footer = {
        SiestaButton(if (isLoading) texts.getString(R.string.ui_signing_in_319) else if (!errorMessage.isNullOrBlank()) texts.getString(R.string.ui_try_again_147) else texts.getString(R.string.ui_sign_in_320),
            onClick = { onGo(phone.trim(), password) },
            enabled = phone.isNotBlank() && password.isNotBlank(), loading = isLoading)
    }) {
        SiestaHeading(if (isLoading) texts.getString(R.string.ui_signing_in_to_your_account_321) else if (!errorMessage.isNullOrBlank()) texts.getString(R.string.ui_could_not_sign_in_322) else texts.getString(R.string.ui_welcome_back_323))
        SiestaBody(texts.getString(R.string.ui_sign_in_to_continue_your_order_your_cart_will_be_saved_324))
        Column { AccountField(phone, { phone = it }, texts.getString(R.string.ui_phone_325), enabled = !isLoading, keyboard = KeyboardType.Phone, externalLabel = true) }
        Column { AccountField(password, { password = it }, texts.getString(R.string.ui_password_326), enabled = !isLoading, keyboard = KeyboardType.Password,
            password = true, externalLabel = true) }
        TextButton(onClick = onForgotPassword, enabled = !isLoading, modifier = Modifier.align(Alignment.End)) {
            Text(texts.getString(R.string.ui_forgot_password_327), color = SiestaAmber)
        }
        if (isLoading) SiestaMessage(texts.getString(R.string.ui_checking_your_details_please_wait_a_moment_328))
        else if (!errorMessage.isNullOrBlank()) SiestaMessage(texts.uiMessage(errorMessage), error = true)
        SiestaButton(texts.getString(R.string.ui_create_account_329), noAccount, secondary = true, enabled = !isLoading)
        SiestaMessage(texts.getString(R.string.ui_you_can_browse_the_menu_without_signing_in_330))
        SiestaButton(texts.getString(R.string.ui_continue_as_guest_331), onBack, secondary = true, enabled = !isLoading)
    }
}
