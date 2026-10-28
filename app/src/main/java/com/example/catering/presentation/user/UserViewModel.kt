package com.example.catering.presentation.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.data.local.biometric.BiometricRestoreResult
import com.example.catering.data.local.biometric.BiometricSessionManager
import com.example.catering.data.local.biometric.BiometricSessionRestorer
import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.model.User
import com.example.catering.domain.usecase.user.ChangePasswordUseCase
import com.example.catering.domain.usecase.user.LoginUserResult
import com.example.catering.domain.usecase.user.LoginUserUseCase
import com.example.catering.domain.usecase.user.LogoutUserResult
import com.example.catering.domain.usecase.user.LogoutUserUseCase
import com.example.catering.domain.usecase.user.PhoneRegistrationUseCase
import com.example.catering.domain.usecase.user.RegisterUserResult
import com.example.catering.domain.usecase.user.RegisterUserUseCase
import com.example.catering.domain.usecase.user.ResendPhoneCodeResult
import com.example.catering.domain.usecase.user.RestoreSessionResult
import com.example.catering.domain.usecase.user.RestoreSessionUseCase
import com.example.catering.domain.usecase.user.UpdateUserAddressResult
import com.example.catering.domain.usecase.user.UpdateUserAddressUseCase
import com.example.catering.domain.usecase.user.UpdateUserNameResult
import com.example.catering.domain.usecase.user.UpdateUserNameUseCase
import com.example.catering.domain.usecase.user.VerifyPhoneResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface ProfileActionStatus {
    data class Success(val action: ProfileActionType,
                       val message: String
    ) : ProfileActionStatus
    data class Error(val message: String) : ProfileActionStatus
}

sealed interface ChangePasswordStatus {
    data object Idle : ChangePasswordStatus
    data object Loading : ChangePasswordStatus
    data object Success : ChangePasswordStatus
    data class Error(val message: String) : ChangePasswordStatus
}

sealed interface BiometricUnlockStatus {
    data object Idle : BiometricUnlockStatus
    data object Loading : BiometricUnlockStatus
    data class Error(
        val message: String
    ) : BiometricUnlockStatus
}

data class PhoneVerificationState(
    val phone: String = "",
    val codeSent: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val resendSeconds: Int = 0
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val updateUserAddressUseCase: UpdateUserAddressUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val biometricSessionManager: BiometricSessionManager,
    private val biometricSessionRestorer: BiometricSessionRestorer,
    private val phoneRegistrationUseCase: PhoneRegistrationUseCase,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isAuthLoading by mutableStateOf(false)
        private set

    var isLoggingOut by mutableStateOf(false)
        private set
    var isSessionRestoring by mutableStateOf(true)
        private set

    private val _phoneVerificationState =
        MutableStateFlow(
            PhoneVerificationState()
        )

    val phoneVerificationState =
        _phoneVerificationState.asStateFlow()

    private var phoneResendJob: Job? = null
    private var restoreInProgress = false
    private val _profileActionStatus =
        MutableStateFlow<ProfileActionStatus?>(value = null)

    private val _changePasswordStatus =
        MutableStateFlow<ChangePasswordStatus>(
            ChangePasswordStatus.Idle
        )

    val changePasswordStatus: StateFlow<ChangePasswordStatus> =
        _changePasswordStatus.asStateFlow()

    var isBiometricUnlockRequired by mutableStateOf(false)
        private set

    var isBiometricEnabled by mutableStateOf(false)
        private set

    var isBiometricSetupBusy by mutableStateOf(false)
        private set

    var biometricSetupError by mutableStateOf<String?>(null)
        private set
    private val _biometricUnlockStatus =
        MutableStateFlow<BiometricUnlockStatus>(
            BiometricUnlockStatus.Idle
        )

    val biometricUnlockStatus: StateFlow<BiometricUnlockStatus> =
        _biometricUnlockStatus.asStateFlow()

    val profileActionStatus: StateFlow<ProfileActionStatus?> = _profileActionStatus.asStateFlow()
    private val _errorMessage =
        MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var passwordChangedAwaitingLogout = false


    suspend fun prepareBiometricUnlockCipher(): Cipher {

        check(
            isBiometricUnlockRequired &&
                    currentUser == null
        ) {
            "Биометрическая разблокировка сейчас не требуется"
        }

        return biometricSessionManager.prepareUnlockCipher()
    }


    suspend fun getProtectedBiometricSessionVersion(): Int {
        check(isBiometricUnlockRequired) {
            "Биометрическая разблокировка сейчас не требуется"
        }

        return biometricSessionManager.getProtectedSessionVersion()
    }

    fun validateAndRegister(
        name: String,
        phone: String,
        password: String,
        passwordRepeat: String,
        onVerificationRequired:
            (phone: String, codeSent: Boolean) -> Unit
    ) {

        if (
            isAuthLoading ||
            isLoggingOut ||
            passwordChangedAwaitingLogout ||
            _changePasswordStatus.value == ChangePasswordStatus.Loading
        ) return

        isAuthLoading = true

        viewModelScope.launch {

            try {

                when (
                    val result = registerUserUseCase.execute(
                        name = name,
                        phone = phone,
                        password = password,
                        passwordRepeat = passwordRepeat
                    )
                ) {

                    is RegisterUserResult.VerificationRequired -> {

                        currentUser = null

                        _errorMessage.value = null

                        onVerificationRequired(
                            result.phone,
                            result.codeSent
                        )
                    }

                    is RegisterUserResult.Error -> {

                        _errorMessage.value =
                            result.message
                    }
                }

            } finally {

                isAuthLoading = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout(onSuccess: () -> Unit = {}) {
        // Запрещаем повторный запуск выхода
        if (isLoggingOut) return

        isLoggingOut = true

        viewModelScope.launch {
            try {
                when (val result = logoutUserUseCase.execute()) {

                    LogoutUserResult.Success -> {
                        currentUser = null
                        _errorMessage.value = null
                        _profileActionStatus.value = null
                        passwordChangedAwaitingLogout = false
                        _changePasswordStatus.value = ChangePasswordStatus.Idle
                        isBiometricUnlockRequired = false
                        _biometricUnlockStatus.value = BiometricUnlockStatus.Idle
                        onSuccess()
                    }

                    is LogoutUserResult.Error -> {
                        _errorMessage.value = result.message

                        _profileActionStatus.value =
                            ProfileActionStatus.Error(
                                message = result.message
                            )
                    }
                }
            } finally {
                isLoggingOut = false
            }
        }
    }


    fun loginUser(
        phone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (
            isAuthLoading ||
            isLoggingOut ||
            passwordChangedAwaitingLogout ||
            _changePasswordStatus.value == ChangePasswordStatus.Loading
        ) return

        isAuthLoading = true

        viewModelScope.launch {

            try {

                when (
                    val result = loginUserUseCase.execute(
                        phone = phone,
                        password = password
                    )
                ) {

                    is LoginUserResult.Success -> {

                        currentUser = result.user
                        _errorMessage.value = null

                        passwordChangedAwaitingLogout = false
                        _changePasswordStatus.value = ChangePasswordStatus.Idle
                        isBiometricUnlockRequired = false
                        _biometricUnlockStatus.value = BiometricUnlockStatus.Idle

                        onSuccess()
                    }

                    is LoginUserResult.Error -> {

                        _errorMessage.value =
                            result.message
                    }
                }

            } finally {

                isAuthLoading = false
            }
        }
    }



    fun restoreUser() {
        if (restoreInProgress) return

        restoreInProgress = true

        viewModelScope.launch {
            isSessionRestoring = true

            try {
                val biometricEnabled =
                    biometricSessionManager.isBiometricEnabled()

                isBiometricEnabled = biometricEnabled

                if (biometricEnabled) {

                    tokenProvider.clearToken()

                    currentUser = null
                    isBiometricUnlockRequired = true
                    _biometricUnlockStatus.value =
                        BiometricUnlockStatus.Idle

                    _errorMessage.value = null

                    return@launch
                }

                isBiometricUnlockRequired = false

                when (val result = restoreSessionUseCase.execute()) {

                    is RestoreSessionResult.Success -> {
                        currentUser = result.currentUser
                        _errorMessage.value = null
                    }

                    is RestoreSessionResult.Error -> {
                        currentUser = null
                        _errorMessage.value = result.message
                    }
                }

            } finally {
                restoreInProgress = false
                isSessionRestoring = false
            }
        }
    }



    fun updateAddress(address: String) {
        viewModelScope.launch {
            when (
                val result = updateUserAddressUseCase.execute(
                    user = currentUser,
                    address = address
                )
            ) {
                is UpdateUserAddressResult.Success -> {
                    currentUser = result.user
                    _profileActionStatus.value = ProfileActionStatus.Success(
                        action = ProfileActionType.ADDRESS,
                        message = "Адрес сохранён")
                }

                is UpdateUserAddressResult.Error -> {
                    _profileActionStatus.value = ProfileActionStatus.Error(message = result.message)
                }
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            when (
                val result = updateUserNameUseCase.execute(
                    user = currentUser,
                    name = name
                )
            ) {
                is UpdateUserNameResult.Success -> {
                    currentUser = result.user
                    _profileActionStatus.value = ProfileActionStatus.Success(
                        action = ProfileActionType.NAME,
                        message = "Имя сохранено")
                }

                is UpdateUserNameResult.Error -> {
                    _profileActionStatus.value = ProfileActionStatus.Error(message = result.message)
                }
            }
        }
    }

    fun clearProfileActionStatus() {
        _profileActionStatus.value = null
    }
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSessionCleared: () -> Unit
    ) {
        if (
            _changePasswordStatus.value == ChangePasswordStatus.Loading ||
            _changePasswordStatus.value == ChangePasswordStatus.Success ||
            isLoggingOut
        ) return

        if (currentUser == null && !passwordChangedAwaitingLogout) {
            _changePasswordStatus.value =
                ChangePasswordStatus.Error(
                    "Необходимо войти в аккаунт"
                )
            return
        }

        _changePasswordStatus.value = ChangePasswordStatus.Loading

        viewModelScope.launch {
            var sessionCleared = false

            try {
                // Запрос на сервер выполняем только один раз.
                if (!passwordChangedAwaitingLogout) {
                    changePasswordUseCase.execute(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )

                    passwordChangedAwaitingLogout = true
                }

                // Сервер уже отозвал старый JWT.
                // Теперь удаляем данные текущего аккаунта.
                when (logoutUserUseCase.execute()) {

                    LogoutUserResult.Success -> {
                        currentUser = null
                        _errorMessage.value = null
                        _profileActionStatus.value = null

                        passwordChangedAwaitingLogout = false
                        _changePasswordStatus.value = ChangePasswordStatus.Success

                        sessionCleared = true
                    }

                    is LogoutUserResult.Error -> {
                        _changePasswordStatus.value =
                            ChangePasswordStatus.Error(
                                "Пароль изменён, но не удалось " +
                                        "завершить выход. " +
                                        "Повторите действие для очистки данных."
                            )
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Проверьте текущий и новый пароли"
                    401 -> "Сессия недействительна. Войдите снова"
                    409 -> "Конфликт изменения пароля"
                    429 -> "Слишком много попыток. Попробуйте позже"
                    in 500..599 -> "Ошибка сервера. Попробуйте позже"
                    else -> "Не удалось изменить пароль"
                }

                _changePasswordStatus.value =
                    ChangePasswordStatus.Error(message)

            } catch (e: IOException) {
                _changePasswordStatus.value =
                    ChangePasswordStatus.Error(
                        "Нет соединения с сервером"
                    )

            } catch (e: Exception) {
                _changePasswordStatus.value =
                    ChangePasswordStatus.Error(
                        "Не удалось завершить операцию"
                    )
            }

            // Навигация только после полной локальной очистки.
            if (sessionCleared) {
                onSessionCleared()
            }
        }
    }

    fun unlockWithBiometrics(
        authenticatedCipher: Cipher,
        onSuccess: () -> Unit
    ) {
        if (
            !isBiometricUnlockRequired ||
            isSessionRestoring ||
            isAuthLoading ||
            isLoggingOut ||
            _biometricUnlockStatus.value ==
            BiometricUnlockStatus.Loading
        ) return

        _biometricUnlockStatus.value =
            BiometricUnlockStatus.Loading

        viewModelScope.launch {
            when (
                val result =
                    biometricSessionRestorer.execute(
                        authenticatedCipher
                    )
            ) {
                is BiometricRestoreResult.Success -> {

                    currentUser = result.user
                    isBiometricUnlockRequired = false

                    _biometricUnlockStatus.value =
                        BiometricUnlockStatus.Idle

                    _errorMessage.value = null

                    onSuccess()
                }

                is BiometricRestoreResult.Error -> {

                    _biometricUnlockStatus.value =
                        BiometricUnlockStatus.Error(
                            result.message
                        )
                }
            }
        }
    }

    fun clearBiometricUnlockError() {
        if (
            _biometricUnlockStatus.value
                    is BiometricUnlockStatus.Error
        ) {
            _biometricUnlockStatus.value =
                BiometricUnlockStatus.Idle
        }
    }

    fun refreshBiometricEnabled() {
        viewModelScope.launch {
            try {
                isBiometricEnabled =
                    biometricSessionManager.isBiometricEnabled()

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                biometricSetupError =
                    "Не удалось проверить настройки биометрии"
            }
        }
    }

    suspend fun prepareBiometricEnableCipher(): Cipher {
        check(
            currentUser != null &&
                    !isBiometricUnlockRequired &&
                    !isSessionRestoring &&
                    !isLoggingOut &&
                    !isAuthLoading &&
                    !isBiometricSetupBusy
        ) {
            "Для включения биометрии нужен активный аккаунт"
        }

        check(!biometricSessionManager.isBiometricEnabled()) {
            "Биометрия уже включена"
        }

        return biometricSessionManager.prepareEnableCipher()
    }

    fun completeBiometricEnable(
        authenticatedCipher: Cipher
    ) {
        if (
            currentUser == null ||
            isBiometricUnlockRequired ||
            isBiometricSetupBusy ||
            isLoggingOut
        ) return

        isBiometricSetupBusy = true
        biometricSetupError = null

        viewModelScope.launch {
            try {
                // Сохраняем JWT зашифрованным.
                // Вызывать только после BiometricPrompt.
                biometricSessionManager.completeEnable(
                    authenticatedCipher
                )

                isBiometricEnabled = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                biometricSetupError =
                    "Не удалось включить вход по отпечатку"
            } finally {
                isBiometricSetupBusy = false
            }
        }
    }

    fun clearBiometricSetupError() {
        biometricSetupError = null
    }
    suspend fun isBiometricMigrationNeeded(): Boolean {
        if (currentUser == null || isBiometricUnlockRequired) {
            return false
        }

        if (!biometricSessionManager.isBiometricEnabled()) {
            return false
        }

        return biometricSessionManager.getProtectedSessionVersion() == 1
    }

    suspend fun prepareBiometricMigrationCipher(): Cipher {
        check(
            currentUser != null &&
                    !isBiometricUnlockRequired &&
                    !isSessionRestoring &&
                    !isLoggingOut &&
                    !isAuthLoading &&
                    !isBiometricSetupBusy
        ) {
            "Обновление защиты сейчас недоступно"
        }

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.R
        ) {
            return biometricSessionManager.prepareCredentialMigrationCipher()
        }

        error(
            "Обновление биометрической защиты требует Android 11 или новее"
        )
    }


    fun completeBiometricMigration(
        authenticatedCipher: Cipher
    ) {
        if (
            currentUser == null ||
            isBiometricUnlockRequired ||
            isSessionRestoring ||
            isLoggingOut ||
            isBiometricSetupBusy
        ) return

        isBiometricSetupBusy = true
        biometricSetupError = null

        viewModelScope.launch {
            try {
                // Проверяем JWT на сервере и
                // переносим его под новый ключ V2.
                biometricSessionManager.completeCredentialMigration(
                    authenticatedCipher
                )

                isBiometricEnabled = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                biometricSetupError =
                    "Не удалось обновить защиту. " +
                            "Повторите попытку."
            } finally {
                isBiometricSetupBusy = false
            }
        }
    }
    suspend fun shouldOfferBiometricEnable(): Boolean {
        if (
            currentUser == null ||
            isBiometricUnlockRequired ||
            isSessionRestoring ||
            isLoggingOut
        ) {
            return false
        }

        if (biometricSessionManager.isBiometricEnabled()) {
            return false
        }

        return !biometricSessionManager
            .isEnableOfferDismissed()
    }

    fun dismissBiometricEnableOffer() {
        viewModelScope.launch {
            biometricSessionManager
                .dismissEnableOffer()
        }
    }
    fun beginPhoneVerification(
        phone: String,
        codeSent: Boolean
    ) {
        phoneResendJob?.cancel()

        _phoneVerificationState.value =
            PhoneVerificationState(
                phone = phone,
                codeSent = codeSent,
                resendSeconds =
                    if (codeSent) 60 else 0
            )

        if (codeSent) {
            startPhoneResendCooldown()
        }
    }

    fun verifyRegistrationPhone(
        code: String,
        onSuccess: () -> Unit
    ) {
        val state =
            _phoneVerificationState.value

        if (
            state.busy ||
            state.phone.isBlank()
        ) {
            return
        }

        _phoneVerificationState.update {
            it.copy(
                busy = true,
                error = null
            )
        }

        viewModelScope.launch {

            try {
                when (
                    val result =
                        phoneRegistrationUseCase.verify(
                            phone = state.phone,
                            code = code
                        )
                ) {

                    is VerifyPhoneResult.Success -> {

                        currentUser = result.user

                        _errorMessage.value = null

                        passwordChangedAwaitingLogout =
                            false

                        _changePasswordStatus.value =
                            ChangePasswordStatus.Idle

                        isBiometricUnlockRequired =
                            false

                        _biometricUnlockStatus.value =
                            BiometricUnlockStatus.Idle

                        onSuccess()
                    }

                    is VerifyPhoneResult.Error -> {
                        _phoneVerificationState.update {
                            it.copy(
                                error = result.message
                            )
                        }
                    }
                }

            } finally {
                _phoneVerificationState.update {
                    it.copy(busy = false)
                }
            }
        }
    }

    fun resendRegistrationPhoneCode() {

        val state =
            _phoneVerificationState.value

        if (
            state.busy ||
            state.phone.isBlank() ||
            state.resendSeconds > 0
        ) {
            return
        }

        _phoneVerificationState.update {
            it.copy(
                busy = true,
                error = null
            )
        }

        viewModelScope.launch {

            try {
                when (
                    val result =
                        phoneRegistrationUseCase
                            .resend(state.phone)
                ) {

                    ResendPhoneCodeResult.Success -> {

                        _phoneVerificationState.update {
                            it.copy(
                                codeSent = true
                            )
                        }

                        startPhoneResendCooldown()
                    }

                    is ResendPhoneCodeResult.Error -> {

                        _phoneVerificationState.update {
                            it.copy(
                                error = result.message
                            )
                        }
                    }
                }

            } finally {
                _phoneVerificationState.update {
                    it.copy(busy = false)
                }
            }
        }
    }

    private fun startPhoneResendCooldown() {

        phoneResendJob?.cancel()

        _phoneVerificationState.update {
            it.copy(resendSeconds = 60)
        }

        phoneResendJob =
            viewModelScope.launch {

                while (
                    _phoneVerificationState
                        .value
                        .resendSeconds > 0
                ) {
                    delay(1000)

                    _phoneVerificationState.update {
                        it.copy(
                            resendSeconds =
                                (it.resendSeconds - 1)
                                    .coerceAtLeast(0)
                        )
                    }
                }
            }
    }
}