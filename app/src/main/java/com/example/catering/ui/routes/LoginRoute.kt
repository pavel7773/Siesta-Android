
package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.LoginScreen

@Composable
fun LoginRoute(
    onBack: () -> Unit,
    userViewModel: UserViewModel,
    noAccount: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val errorMessage by userViewModel.errorMessage
        .collectAsStateWithLifecycle()

    LoginScreen(
        onBack = onBack,

        onGo = { phone, password ->
            userViewModel.loginUser(
                phone = phone,
                password = password,
                onSuccess = onLoginSuccess
            )
        },

        noAccount = {
            userViewModel.clearError()
            noAccount()
        },

        onForgotPassword = {
            userViewModel.clearError()
            onForgotPassword()
        },

        isLoading = userViewModel.isAuthLoading,
        errorMessage = errorMessage
    )
}
