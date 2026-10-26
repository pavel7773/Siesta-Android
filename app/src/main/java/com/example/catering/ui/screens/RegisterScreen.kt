package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.example.catering.ui.components.common.*

@Composable
fun RegisterScreen(onBack: () -> Unit,
    onRegisterClick: (name: String, phone: String, password: String, passRepeat: String) -> Unit,
    errorMessage: String?, isLoading: Boolean = false, onGoLogin: () -> Unit = onBack) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    SiestaPage(texts.getString(R.string.ui_create_account_434), onBack, footer = {
        SiestaButton(if (isLoading) texts.getString(R.string.ui_creating_account_435) else texts.getString(R.string.ui_create_account_329),
            { onRegisterClick(name, phone, password, repeat) }, loading = isLoading)
    }) {
        SiestaHeading(texts.getString(R.string.ui_let_us_get_to_know_you_436))
        SiestaBody(texts.getString(R.string.ui_one_account_for_your_orders_437))
        Column { AccountField(name, { name = it }, texts.getString(R.string.ui_name_215), enabled = !isLoading, externalLabel = true) }
        Column { AccountField(phone, { phone = it }, texts.getString(R.string.ui_phone_325), enabled = !isLoading, keyboard = KeyboardType.Phone, externalLabel = true) }
        Column { AccountField(password, { password = it }, texts.getString(R.string.ui_password_326), enabled = !isLoading, keyboard = KeyboardType.Password, password = true, externalLabel = true) }
        Column { AccountField(repeat, { repeat = it }, texts.getString(R.string.ui_confirm_password_438), enabled = !isLoading, keyboard = KeyboardType.Password, password = true, externalLabel = true) }
        if (!isLoading && !errorMessage.isNullOrBlank()) SiestaMessage(texts.uiMessage(errorMessage), error = true)
        SiestaButton(texts.getString(R.string.ui_already_have_an_account_439), onGoLogin, secondary = true, enabled = !isLoading)
    }
}
