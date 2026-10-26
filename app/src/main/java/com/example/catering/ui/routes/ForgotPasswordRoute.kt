package com.example.catering.ui.routes


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.PasswordRecoveryStatus
import com.example.catering.presentation.user.PasswordRecoveryViewModel
import com.example.catering.ui.screens.ForgotPasswordScreen

@Composable
fun ForgotPasswordRoute(
    viewModel: PasswordRecoveryViewModel,
    onBack: () -> Unit,
    onCodeRequested: () -> Unit
) {
    val status by viewModel.requestCodeStatus
        .collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (status == PasswordRecoveryStatus.Success) {
            viewModel.clearRequestCodeStatus()
            onCodeRequested()
        }
    }

    ForgotPasswordScreen(
        onBack = onBack,
        onRequestCode = { email ->
            viewModel.requestCode(email)
        },
        isSubmitting =
            status == PasswordRecoveryStatus.Loading,
        errorMessage =
            (status as? PasswordRecoveryStatus.Error)?.message
    )
}
