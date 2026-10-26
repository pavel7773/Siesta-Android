package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.catering.ui.components.common.AccountAction
import com.example.catering.ui.components.common.AccountCopy
import com.example.catering.ui.components.common.AccountField
import com.example.catering.ui.components.common.AccountForm

@Composable
fun ResetPasswordScreen(email: String, busy: Boolean, success: Boolean, error: String?,
                        resendSeconds: Int, message: String?, onBack: () -> Unit,
                        onResend: () -> Unit, onSubmit: (String, String) -> Unit, onGoLogin: () -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    // Passwords and codes are deliberately not persisted in saved instance state.
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    AccountForm(texts.getString(R.string.ui_new_password_192), onBack) {
        if (success) {
            AccountCopy(texts.getString(R.string.ui_password_changed_sign_in_with_your_new_password_previous_s_440))
            AccountAction(texts.getString(R.string.ui_go_to_sign_in_441), onClick = onGoLogin)
        } else {
            AccountCopy(texts.getString(R.string.ui_if_is_verified_an_email_with_a_code_will_be_sent_check_you_442, email))
            AccountField(code, { code = it.filter { c -> c in '0'..'9' }.take(6) }, texts.getString(R.string.ui_six_digit_code_258), !busy, KeyboardType.NumberPassword)
            AccountField(password, { password = it }, texts.getString(R.string.ui_new_password_192), !busy, KeyboardType.Password, password = true)
            AccountField(repeat, { repeat = it }, texts.getString(R.string.ui_confirm_password_443), !busy, KeyboardType.Password, password = true)
            AccountCopy(texts.getString(R.string.ui_between_6_and_128_characters_use_a_unique_password_444))
            if (repeat.isNotEmpty() && repeat != password) AccountCopy(texts.getString(R.string.ui_passwords_do_not_match_28), error = true)
            error?.let { AccountCopy(texts.uiMessage(it), error = true) }
            message?.let { AccountCopy(texts.uiMessage(it)) }
            AccountAction(if (busy) texts.getString(R.string.ui_processing_request_445) else texts.getString(R.string.ui_set_password_446),
                !busy && code.length == 6 && password.length in 6..128 && password == repeat,
                onClick = { onSubmit(code, password) })
            AccountAction(if (resendSeconds > 0) texts.getString(R.string.ui_resend_in_s_447, resendSeconds) else texts.getString(R.string.ui_resend_code_448),
                !busy && resendSeconds == 0, secondary = true, onClick = onResend)
            AccountAction(texts.getString(R.string.ui_change_email_449), !busy, secondary = true, onClick = onBack)
        }
    }
}
