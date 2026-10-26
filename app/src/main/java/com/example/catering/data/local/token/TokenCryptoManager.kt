package com.example.catering.data.local.token

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class TokenCryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS = "siesta_session_token_key_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_TAG_BITS = 128
    }

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    private fun existingKey(): SecretKey? =
        keyStore().getKey(KEY_ALIAS, null) as? SecretKey

    private fun createKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .build()

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        ).apply {
            init(spec)
        }.generateKey()
    }

    fun encrypt(data: ByteArray): EncryptedTokenData {
        val key = existingKey() ?: createKey()

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }

        return EncryptedTokenData(
            ciphertext = cipher.doFinal(data),
            iv = cipher.iv
        )
    }

    fun decrypt(
        encryptedData: EncryptedTokenData
    ): ByteArray {
        val key = existingKey()
            ?: error("Ключ обычной сессии не найден")

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(
                    IV_TAG_BITS,
                    encryptedData.iv
                )
            )
        }

        return cipher.doFinal(
            encryptedData.ciphertext
        )
    }

    fun deleteKey() {
        val store = keyStore()

        if (store.containsAlias(KEY_ALIAS)) {
            store.deleteEntry(KEY_ALIAS)
        }
    }
}

data class EncryptedTokenData(
    val ciphertext: ByteArray,
    val iv: ByteArray
)