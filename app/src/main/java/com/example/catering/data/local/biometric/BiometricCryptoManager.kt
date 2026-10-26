package com.example.catering.data.local.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class BiometricCryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS = "siesta_biometric_key_v1"
        private const val CREDENTIAL_KEY_ALIAS = "siesta_biometric_key_v2"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_TAG_BITS = 128
    }

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    private fun createKey(): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        generator.init(builder.build())

        return generator.generateKey()
    }

    private fun existingKey(): SecretKey? =
        keyStore().getKey(KEY_ALIAS, null) as? SecretKey

    fun createEncryptionCipher(): Cipher {
        val key = existingKey() ?: createKey()

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    fun createDecryptionCipher(iv: ByteArray): Cipher {
        val key = existingKey()
            ?: error("Биометрический ключ не найден")

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(IV_TAG_BITS, iv)
            )
        }
    }

    // Вызывать только после успешного BiometricPrompt.
    fun encryptAuthenticated(
        cipher: Cipher,
        data: ByteArray
    ): EncryptedData {
        val encrypted = cipher.doFinal(data)

        return EncryptedData(
            ciphertext = encrypted,
            iv = cipher.iv
        )
    }

    // Вызывать только после успешного BiometricPrompt.
    fun decryptAuthenticated(
        cipher: Cipher,
       data: ByteArray
    ): ByteArray {
        return cipher.doFinal(data)
    }


    private fun existingCredentialKey(): SecretKey? {
        return keyStore().getKey(
            CREDENTIAL_KEY_ALIAS,
            null
        ) as? SecretKey
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createCredentialKey(): SecretKey {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "Разблокировка с PIN требует Android 11 или новее"
        }

        val spec = KeyGenParameterSpec.Builder(
            CREDENTIAL_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG or
                        KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
            .build()

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        ).apply {
            init(spec)
        }.generateKey()
    }

    // Новый ключ: отпечаток ИЛИ PIN устройства.
    @RequiresApi(Build.VERSION_CODES.R)
    fun createCredentialEncryptionCipher(): Cipher {
        val key = existingCredentialKey()
            ?: createCredentialKey()

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    fun createCredentialDecryptionCipher(
        iv: ByteArray
    ): Cipher {
        val key = existingCredentialKey()
            ?: error("Защищённый ключ не найден")

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(IV_TAG_BITS, iv)
            )
        }
    }

    fun deleteKey() {
        val store = keyStore()

        for (alias in listOf(KEY_ALIAS, CREDENTIAL_KEY_ALIAS)) {
            if (store.containsAlias(alias)) {
                store.deleteEntry(alias)
            }
        }
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray
)
