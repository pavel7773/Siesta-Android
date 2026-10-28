package com.example.catering.presentation.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.usecase.password.PasswordRecoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface PasswordRecoveryStatus {
    data object Idle : PasswordRecoveryStatus
    data object Loading : PasswordRecoveryStatus
    data object Success : PasswordRecoveryStatus
    data class Error(
        val message: String
    ) : PasswordRecoveryStatus
}

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
    private val passwordRecoveryUseCase: PasswordRecoveryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val requestedEmail: StateFlow<String?> = savedStateHandle.getStateFlow("recovery_email", null)
    private val _resendSeconds = MutableStateFlow(0)
    val resendSeconds = _resendSeconds.asStateFlow()

    private val _requestCodeStatus =
        MutableStateFlow<PasswordRecoveryStatus>(
            PasswordRecoveryStatus.Idle
        )

    val requestCodeStatus: StateFlow<PasswordRecoveryStatus> =
        _requestCodeStatus.asStateFlow()

    private val _resetPasswordStatus =
        MutableStateFlow<PasswordRecoveryStatus>(
            PasswordRecoveryStatus.Idle
        )

    val resetPasswordStatus: StateFlow<PasswordRecoveryStatus> =
        _resetPasswordStatus.asStateFlow()

    private fun isBusy(): Boolean {
        return _requestCodeStatus.value ==
                PasswordRecoveryStatus.Loading ||
                _resetPasswordStatus.value ==
                PasswordRecoveryStatus.Loading
    }

    // Запрос кода восстановления.
    fun requestCode(email: String) {
        if (isBusy() || _resendSeconds.value > 0) return

        _requestCodeStatus.value =
            PasswordRecoveryStatus.Loading

        viewModelScope.launch {
            try {
                passwordRecoveryUseCase.requestCode(email)
                savedStateHandle["recovery_email"] = email.trim().lowercase(java.util.Locale.ROOT)
                _resendSeconds.value = 60
                viewModelScope.launch {
                    while (_resendSeconds.value > 0) {
                        delay(1000)
                        _resendSeconds.value = (_resendSeconds.value - 1).coerceAtLeast(0)
                    }
                }

                // Сервер возвращает 202 даже для
                // неизвестного адреса.
                _requestCodeStatus.value =
                    PasswordRecoveryStatus.Success

            } catch (e: CancellationException) {
                throw e

            } catch (e: IllegalArgumentException) {
                _requestCodeStatus.value =
                    PasswordRecoveryStatus.Error(
                        e.message ?: "Проверьте email"
                    )

            } catch (e: HttpException) {
                _requestCodeStatus.value =
                    PasswordRecoveryStatus.Error(
                        httpErrorMessage(e)
                    )

            } catch (e: IOException) {
                _requestCodeStatus.value =
                    PasswordRecoveryStatus.Error(
                        "Нет соединения с сервером"
                    )

            } catch (e: Exception) {
                _requestCodeStatus.value =
                    PasswordRecoveryStatus.Error(
                        "Не удалось выполнить запрос"
                    )
            }
        }
    }

    // Проверка кода и установка нового пароля.
    fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        if (isBusy()) return

        _resetPasswordStatus.value =
            PasswordRecoveryStatus.Loading

        viewModelScope.launch {
            try {
                passwordRecoveryUseCase.resetPassword(
                    email = email,
                    code = code,
                    newPassword = newPassword
                )

                _resetPasswordStatus.value =
                    PasswordRecoveryStatus.Success

            } catch (e: CancellationException) {
                throw e

            } catch (e: IllegalArgumentException) {
                _resetPasswordStatus.value =
                    PasswordRecoveryStatus.Error(
                        e.message ?: "Проверьте данные"
                    )

            } catch (e: HttpException) {
                _resetPasswordStatus.value =
                    PasswordRecoveryStatus.Error(
                        httpErrorMessage(e)
                    )

            } catch (e: IOException) {
                _resetPasswordStatus.value =
                    PasswordRecoveryStatus.Error(
                        "Нет соединения с сервером"
                    )

            } catch (e: Exception) {
                _resetPasswordStatus.value =
                    PasswordRecoveryStatus.Error(
                        "Не удалось изменить пароль"
                    )
            }
        }
    }

    private fun httpErrorMessage(
        error: HttpException
    ): String {
        return when (error.code()) {
            400 -> "Проверьте данные или код подтверждения"
            401 -> "Сессия недействительна"
            429 -> "Слишком много попыток. Попробуйте позже"
            503 -> "Сервис временно недоступен"
            in 500..599 -> "Ошибка сервера. Попробуйте позже"
            else -> "Не удалось выполнить запрос"
        }
    }

    fun clearRequestCodeStatus() {
        if (_requestCodeStatus.value !=
            PasswordRecoveryStatus.Loading
        ) {
            _requestCodeStatus.value =
                PasswordRecoveryStatus.Idle
        }
    }
}
