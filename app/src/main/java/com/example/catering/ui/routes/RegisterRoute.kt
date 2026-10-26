package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.RegisterScreen

@Composable
fun RegisterRoute(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onGoLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val errorMessage by userViewModel.errorMessage.collectAsStateWithLifecycle()

    RegisterScreen(
        onGoLogin = { userViewModel.clearError(); onGoLogin() },
        onBack = {
            userViewModel.clearError()
            onBack()
        },
        isLoading = userViewModel.isAuthLoading,
        errorMessage = errorMessage,
        onRegisterClick = { name, phone, password, passwordRepeat ->
            userViewModel.validateAndRegister(
                name = name,
                phone = phone,
                password = password,
                passwordRepeat = passwordRepeat,
                onSuccess = onRegisterSuccess
            )
        }
    )
}