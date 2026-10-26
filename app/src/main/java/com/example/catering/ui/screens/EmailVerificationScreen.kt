package com.example.catering.ui.screens

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.catering.domain.model.EmailStatus
import com.example.catering.ui.components.common.AccountAction
import com.example.catering.ui.components.common.AccountCopy
import com.example.catering.ui.components.common.AccountField
import com.example.catering.ui.components.common.AccountForm

@Composable
fun EmailVerificationScreen(
    status: EmailStatus?, busy: Boolean, error: String?, message: String?,
    resendSeconds: Int, onBack: () -> Unit, onReload: () -> Unit,
    onSave: (String) -> Unit, onVerify: (String) -> Unit, onResend: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var email by rememberSaveable(status?.email) { mutableStateOf(status?.email.orEmpty()) }
    var code by remember(status?.email) { mutableStateOf("") }
    AccountForm(texts.getString(R.string.ui_email_address_249), onBack) {
        if (status == null) {
            AccountCopy(if (busy) texts.getString(R.string.ui_loading_details_250) else texts.getString(R.string.ui_could_not_retrieve_email_status_251))
            error?.let { AccountCopy(texts.uiMessage(it), error = true) }
            AccountAction(texts.getString(R.string.ui_retry_141), enabled = !busy, onClick = onReload)
        } else {
            AccountCopy(
                if (status.verified) texts.getString(R.string.ui_email_verified_you_can_use_it_to_recover_your_account_252)
                else texts.getString(R.string.ui_add_and_verify_your_email_address_to_recover_your_account_253)
            )
            AccountField(email, { email = it }, texts.getString(R.string.ui_email_address_249), !busy, KeyboardType.Email)
            val normalized = email.trim().lowercase(java.util.Locale.ROOT)
            val valid =
                normalized.length in 5..254 && Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""").matches(
                    normalized
                )
            val changed = normalized != status.email.orEmpty()
            if (changed || status.email.isNullOrBlank()) {
                AccountCopy(texts.getString(R.string.ui_after_changing_your_email_address_you_will_need_to_verify_254))
                AccountAction(
                    if (busy) texts.getString(R.string.ui_saving_255) else texts.getString(R.string.ui_save_and_send_code_256), !busy && valid,
                    onClick = { onSave(email) })
            } else if (!status.verified) {
                AccountCopy(texts.getString(R.string.ui_enter_the_code_from_your_email_it_is_valid_for_10_minutes_257))
                AccountField(
                    code, { code = it.filter { c -> c in '0'..'9' }.take(6) },
                    texts.getString(R.string.ui_six_digit_code_258), !busy, KeyboardType.NumberPassword
                )
                AccountAction(
                    if (busy) texts.getString(R.string.ui_verifying_259) else texts.getString(R.string.ui_verify_email_260), !busy && code.length == 6,
                    onClick = { onVerify(code) })
                AccountAction(
                    if (resendSeconds > 0) texts.getString(R.string.ui_resend_in_s_261, resendSeconds) else texts.getString(R.string.ui_resend_code_262),
                    !busy && resendSeconds == 0, secondary = true, onClick = onResend
                )
            }
            message?.let { AccountCopy(texts.uiMessage(it)) }
            error?.let { AccountCopy(texts.uiMessage(it), error = true) }
            AccountAction(texts.getString(R.string.ui_refresh_status_263), !busy, secondary = true, onClick = onReload)
        }
    }
}
