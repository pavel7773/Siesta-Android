
package com.example.catering.data.local.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class BiometricAuthenticator(
    private val activity: FragmentActivity
) {

    fun authenticate(
        cipher: Cipher,
        title: String,
        subtitle: String,
        onSuccess: (Cipher) -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit,
        allowDeviceCredential: Boolean = false
    ) {

        // PIN + CryptoObject поддерживаем на Android 11+.
        if (
            allowDeviceCredential &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R
        ) {
            onError(
                "Вход по PIN-коду доступен на Android 11 и новее"
            )
            return
        }

        val authenticators = if (allowDeviceCredential) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        val availability = BiometricManager.from(activity)
            .canAuthenticate(authenticators)

        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            val message = when (availability) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "На устройстве нет биометрического датчика"

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "Настройте отпечаток или блокировку экрана Android"

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    "Биометрическая проверка временно недоступна"

                else ->
                    "Этот способ разблокировки недоступен"
            }

            onError(message)
            return
        }

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        // Системный PIN заменяет кнопку «Отмена».
        // Одновременно использовать их нельзя.
        if (!allowDeviceCredential) {
            builder.setNegativeButtonText("Отмена")
        }

        val promptInfo = builder.build()

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    val verifiedCipher =
                        result.cryptoObject?.cipher

                    if (verifiedCipher == null) {
                        onError(
                            "Не удалось подтвердить защищённую операцию"
                        )
                        return
                    }

                    onSuccess(verifiedCipher)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(
                        errorCode,
                        errString
                    )

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            onCancelled()
                        }

                        else -> {
                            onError(errString.toString())
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Неверный отпечаток:
                    // Android позволяет повторить попытку.
                }
            }
        )

        prompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(cipher)
        )
    }
}
