package com.example.catering.ui.screens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.catering.R
import com.example.catering.presentation.localization.uiMessage
import com.example.catering.ui.components.common.AccountAction
import com.example.catering.ui.components.common.AccountCopy
import com.example.catering.ui.components.common.AccountField
import com.example.catering.ui.components.common.AccountForm

@Composable
fun PhoneVerificationScreen(
    phone: String,
    codeSent: Boolean,
    busy: Boolean,
    error: String?,
    resendSeconds: Int,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    onResend: () -> Unit
) {
    val texts =
        androidx.compose.ui.platform
            .LocalContext.current.resources

    var code by rememberSaveable {
        mutableStateOf("")
    }

    AccountForm(
        texts.getString(
            R.string.ui_phone_verification_title
        ),
        onBack
    ) {

        AccountCopy(
            if (codeSent) {
                texts.getString(
                    R.string.ui_phone_verification_code_sent,
                    "+$phone"
                )
            } else {
                texts.getString(
                    R.string.ui_phone_verification_code_not_sent
                )
            }
        )

        AccountField(
            value = code,
            onChange = {
                code =
                    it.filter { char ->
                        char.isDigit()
                    }.take(6)
            },
            label = texts.getString(
                R.string.ui_phone_verification_code
            ),
            enabled = !busy,
            keyboard = KeyboardType.NumberPassword
        )

        AccountAction(
            text =
                if (busy) {
                    texts.getString(
                        R.string.ui_phone_verification_verifying
                    )
                } else {
                    texts.getString(
                        R.string.ui_phone_verification_confirm
                    )
                },
            enabled =
                !busy &&
                        codeSent &&
                        code.length == 6,
            onClick = {
                onVerify(code)
            }
        )

        AccountAction(
            text =
                if (resendSeconds > 0) {
                    texts.getString(
                        R.string.ui_phone_verification_resend_in,
                        resendSeconds
                    )
                } else {
                    texts.getString(
                        R.string.ui_phone_verification_resend
                    )
                },
            enabled =
                !busy &&
                        resendSeconds == 0,
            secondary = true,
            onClick = onResend
        )

        error?.let {
            AccountCopy(
                texts.uiMessage(it),
                error = true
            )
        }
    }
}