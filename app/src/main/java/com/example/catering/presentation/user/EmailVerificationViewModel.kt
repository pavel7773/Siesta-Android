package com.example.catering.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.EmailStatus
import com.example.catering.domain.usecase.user.EmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class EmailVerificationState(
    val status: EmailStatus? = null,
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val resendSeconds: Int = 0
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val useCase: EmailVerificationUseCase
) : ViewModel() {
    private val mutableState = MutableStateFlow(EmailVerificationState())
    val state = mutableState.asStateFlow()

    init { reload() }

    fun reload() = runAction { refresh() }

    fun save(email: String) = runAction {
        useCase.save(email)
        refresh()
        if (mutableState.value.status?.verified == false) send()
        else mutableState.update { it.copy(message = "Этот email уже подтверждён") }
    }

    fun resend() {
        if (mutableState.value.resendSeconds > 0) return
        runAction { send() }
    }

    fun verify(code: String) = runAction {
        useCase.verify(code)
        refresh()
        mutableState.update { it.copy(message = "Электронная почта подтверждена") }
    }

    private suspend fun refresh() {
        val status = useCase.status()
        mutableState.update { it.copy(status = status) }
    }

    private suspend fun send() {
        useCase.sendCode()
        mutableState.update { it.copy(message = "Код отправлен. Проверьте также папку «Спам».") }
        startCooldown()
    }

    private fun startCooldown() {
        if (mutableState.value.resendSeconds > 0) return
        mutableState.update { it.copy(resendSeconds = 60) }
        viewModelScope.launch {
            while (mutableState.value.resendSeconds > 0) {
                delay(1000)
                mutableState.update { it.copy(resendSeconds = (it.resendSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun runAction(action: suspend () -> Unit) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when (e) {
                    is IllegalArgumentException -> e.message ?: "Проверьте данные"
                    is IOException -> "Нет соединения. Проверьте интернет и повторите."
                    is HttpException -> when (e.code()) {
                        400 -> "Неверный или истёкший код. Проверьте ввод или запросите новый."
                        401 -> "Сессия истекла. Войдите в аккаунт снова."
                        409 -> "Адрес уже подтверждён или занят; повторная отправка доступна через минуту."
                        429 -> "Слишком много попыток. Попробуйте позже."
                        404 -> "Подтверждение email пока недоступно на сервере."
                        else -> "Сервис временно недоступен. Попробуйте позже."
                    }
                    else -> "Не удалось выполнить действие. Повторите попытку."
                }
                if (e is HttpException && e.code() == 409) startCooldown()
                mutableState.update { it.copy(error = message) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
