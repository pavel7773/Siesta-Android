package com.example.catering.ui.routes

import com.example.catering.presentation.localization.uiMessage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.presentation.user.ProfileActionStatus
import com.example.catering.presentation.user.ProfileActionType
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.PersonalDataScreen

@Composable
fun PersonalDataRoute(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val actionStatus by userViewModel
        .profileActionStatus
        .collectAsStateWithLifecycle()

    var isSaving by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(actionStatus) {
        if (!isSaving) return@LaunchedEffect

        when (val status = actionStatus) {

            is ProfileActionStatus.Success -> {
                if (status.action == ProfileActionType.NAME) {
                    isSaving = false

                    userViewModel.clearProfileActionStatus()

                    // Возвращаемся в профиль
                    // после подтверждённого сохранения.
                    onBack()
                }
            }

            is ProfileActionStatus.Error -> {
                isSaving = false

                userViewModel.clearProfileActionStatus()

                snackbarHostState.showSnackbar(
                    message = texts.uiMessage(status.message)
                )
            }

            null -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PersonalDataScreen(
            user = userViewModel.currentUser,
            onBack = onBack,
            onSaveName = { newName ->
                if (!isSaving) {
                    userViewModel.clearProfileActionStatus()
                    isSaving = true
                    userViewModel.updateName(newName)
                }
            },
            isSaving = isSaving
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )
    }
}
