package com.example.catering.ui.routes


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.EmailVerificationViewModel
import com.example.catering.ui.screens.EmailVerificationScreen

@Composable
fun EmailVerificationRoute(
    onBack: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmailVerificationScreen(
        status = state.status,
        busy = state.busy,
        error = state.error,
        message = state.message,
        resendSeconds = state.resendSeconds,
        onBack = onBack,
        onReload = viewModel::reload,
        onSave = viewModel::save,
        onVerify = viewModel::verify,
        onResend = viewModel::resend
    )
}
