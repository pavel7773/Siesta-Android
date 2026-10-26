package com.example.catering.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.catering.presentation.user.ChangePasswordStatus
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.ChangePasswordScreen

@Composable
fun ChangePasswordRoute(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onSessionCleared: () -> Unit
) {
    val status by userViewModel
        .changePasswordStatus
        .collectAsState()

    ChangePasswordScreen(
        user = userViewModel.currentUser,

        onBack = onBack,

        onChangePassword = { currentPassword, newPassword ->
            userViewModel.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                onSessionCleared = onSessionCleared
            )
        },

        isSubmitting = status == ChangePasswordStatus.Loading,

        errorMessage = when (val currentStatus = status) {
            is ChangePasswordStatus.Error -> currentStatus.message
            else -> null
        }
    )
}
