package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.PhoneVerificationScreen

@Composable
fun PhoneVerificationRoute(
    phone: String,
    codeSent: Boolean,
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    val state by
    userViewModel
        .phoneVerificationState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        phone,
        codeSent
    ) {
        userViewModel.beginPhoneVerification(
            phone = phone,
            codeSent = codeSent
        )
    }

    PhoneVerificationScreen(
        phone = state.phone.ifBlank {
            phone
        },
        codeSent = state.codeSent,
        busy = state.busy,
        error = state.error,
        resendSeconds =
            state.resendSeconds,
        onBack = onBack,
        onVerify = { code ->
            userViewModel
                .verifyRegistrationPhone(
                    code = code,
                    onSuccess = onVerified
                )
        },
        onResend =
            userViewModel::
            resendRegistrationPhoneCode
    )
}