
package com.example.catering.ui.routes

import com.example.catering.R

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catering.data.local.biometric.BiometricAuthenticator
import com.example.catering.presentation.user.BiometricUnlockStatus
import com.example.catering.presentation.user.ProfileActionStatus
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.screens.BiometricUnlockScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.crypto.Cipher

@Composable
fun BiometricUnlockRoute(
    userViewModel: UserViewModel,
    prepareUnlockCipher: suspend () -> Cipher,
    onUnlocked: () -> Unit,
    onPasswordLogin: () -> Unit,
    getProtectedSessionVersion: suspend () -> Int = { 1 }
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val context = LocalContext.current
    val activity = remember(context) {
        context.findFragmentActivity()
    }

    val scope = rememberCoroutineScope()

    val unlockStatus by userViewModel
        .biometricUnlockStatus
        .collectAsStateWithLifecycle()

    val profileStatus by userViewModel
        .profileActionStatus
        .collectAsStateWithLifecycle()

    var localError by remember {
        mutableStateOf<String?>(null)
    }

    var isPreparing by remember {
        mutableStateOf(false)
    }

    var isPromptVisible by remember {
        mutableStateOf(false)
    }

    var showLogoutConfirmation by remember {
        mutableStateOf(false)
    }

    val isBusy =
        isPreparing ||
                isPromptVisible ||
                userViewModel.isLoggingOut ||
                unlockStatus == BiometricUnlockStatus.Loading

    val errorMessage =
        localError
            ?: (unlockStatus as? BiometricUnlockStatus.Error)?.message
            ?: (profileStatus as? ProfileActionStatus.Error)?.message

    BiometricUnlockScreen(
        isBusy = isBusy,
        errorMessage = errorMessage,

        onUnlockClick = {
            if (!isBusy) {
                localError = null
                userViewModel.clearBiometricUnlockError()

                if (activity == null) {
                    localError =
                        texts.getString(R.string.ui_could_not_open_system_biometric_verification_123)
                } else {
                    isPreparing = true

                    scope.launch {
                        try {
                            val version = getProtectedSessionVersion()

                            check(version == 1 || version == 2) {
                                "Неизвестная версия защищённой сессии"
                            }

                            val cipher = prepareUnlockCipher()

                            isPreparing = false
                            isPromptVisible = true

                            val authenticator =
                                BiometricAuthenticator(activity)

                            authenticator.authenticate(
                                cipher = cipher,
                                title = texts.getString(R.string.ui_sign_in_to_siesta_125),
                                subtitle = texts.getString(R.string.ui_confirm_with_your_fingerprint_126),

                                onSuccess = { verifiedCipher ->
                                    isPromptVisible = false

                                    userViewModel.unlockWithBiometrics(
                                        authenticatedCipher =
                                            verifiedCipher,
                                        onSuccess = onUnlocked
                                    )
                                },

                                onCancelled = {
                                    isPromptVisible = false
                                },

                                onError = { message ->
                                    isPromptVisible = false
                                    localError = message
                                },

                                allowDeviceCredential = version == 2
                            )

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {
                            isPreparing = false
                            isPromptVisible = false

                            localError =
                                texts.getString(R.string.ui_could_not_prepare_127) +
                                        texts.getString(R.string.ui_biometric_verification_128)
                        }
                    }
                }
            }
        },

        onUsePasswordClick = {
            if (!isBusy) {
                showLogoutConfirmation = true
            }
        }
    )

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showLogoutConfirmation = false
            },
            title = {
                Text(texts.getString(R.string.ui_end_your_current_session_129))
            },
            text = {
                Text(
                    texts.getString(R.string.ui_this_will_remove_your_saved_session_and_130) +
                            texts.getString(R.string.ui_turn_off_fingerprint_sign_in_131) +
                            texts.getString(R.string.ui_you_will_only_need_to_enter_your_phone_number_and_password_132) +
                            texts.getString(R.string.ui_again_after_signing_out_133) +
                            texts.getString(R.string.ui_to_unlock_the_app_normally_134) +
                            texts.getString(R.string.ui_use_your_fingerprint_or_device_pin_135)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        showLogoutConfirmation = false

                        userViewModel.clearProfileActionStatus()

                        userViewModel.logout(
                            onSuccess = onPasswordLogin
                        )
                    }
                ) {
                    Text(texts.getString(R.string.ui_sign_out_136))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                    }
                ) {
                    Text(texts.getString(R.string.ui_stay_signed_in_137))
                }
            }
        )
    }

}
// Находим FragmentActivity даже тогда,
// когда Compose использует ContextWrapper.

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context = this

    while (true) {
        if (current is FragmentActivity) {
            return current
        }

        current =
            (current as? ContextWrapper)?.baseContext
                ?: return null
    }
}
