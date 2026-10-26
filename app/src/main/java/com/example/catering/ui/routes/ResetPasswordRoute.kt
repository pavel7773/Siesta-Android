package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.PasswordRecoveryStatus
import com.example.catering.presentation.user.PasswordRecoveryViewModel
import com.example.catering.ui.screens.ResetPasswordScreen

@Composable
fun ResetPasswordRoute(
    viewModel: PasswordRecoveryViewModel,
    onBack: () -> Unit,
    onGoLogin: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val email by viewModel.requestedEmail
        .collectAsStateWithLifecycle()

    val requestStatus by viewModel.requestCodeStatus
        .collectAsStateWithLifecycle()

    val resetStatus by viewModel.resetPasswordStatus
        .collectAsStateWithLifecycle()

    val resendSeconds by viewModel.resendSeconds
        .collectAsStateWithLifecycle()

    val currentEmail = email

    if (currentEmail == null) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val busy =
        requestStatus == PasswordRecoveryStatus.Loading ||
                resetStatus == PasswordRecoveryStatus.Loading

    val error =
        (resetStatus as? PasswordRecoveryStatus.Error)?.message
            ?: (requestStatus as? PasswordRecoveryStatus.Error)?.message

    val message =
        if (requestStatus == PasswordRecoveryStatus.Success) {
            texts.getString(R.string.ui_if_the_address_is_verified_we_will_send_an_email_with_a_co_156)
        } else {
            null
        }

    ResetPasswordScreen(
        email = currentEmail,
        busy = busy,
        success = resetStatus == PasswordRecoveryStatus.Success,
        error = error,
        resendSeconds = resendSeconds,
        message = message,
        onBack = onBack,
        onResend = {
            if (!busy && resendSeconds == 0) {
                viewModel.requestCode(currentEmail)
            }
        },
        onSubmit = { code, password ->
            viewModel.resetPassword(
                email = currentEmail,
                code = code,
                newPassword = password
            )
        },
        onGoLogin = onGoLogin
    )
}
