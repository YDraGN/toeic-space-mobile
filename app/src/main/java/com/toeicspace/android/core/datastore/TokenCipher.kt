package com.toeicspace.android.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** Encrypts secrets before they reach disk. */
interface TokenCipher {
    fun encrypt(plainText: String): String

    /** Returns null when the data cannot be decrypted, e.g. after the key was lost. */
    fun decrypt(cipherText: String): String?
}

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val KEY_ALIAS = "toeic_space_session_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_SIZE_BYTES = 12
private const val TAG_SIZE_BITS = 128

/**
 * AES-256-GCM with a key that never leaves the Android Keystore.
 * Output is Base64 of `IV || ciphertext`.
 */
@Singleton
class KeystoreTokenCipher
    @Inject
    constructor() : TokenCipher {
        override fun encrypt(plainText: String): String {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
        }

        override fun decrypt(cipherText: String): String? =
            try {
                val bytes = Base64.decode(cipherText, Base64.NO_WRAP)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(TAG_SIZE_BITS, bytes, 0, IV_SIZE_BYTES),
                )
                String(
                    cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES),
                    Charsets.UTF_8,
                )
            } catch (_: GeneralSecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun getOrCreateKey(): SecretKey {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (existing != null) return existing.secretKey

            val generator =
                KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER,
                )
            generator.init(
                KeyGenParameterSpec
                    .Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            return generator.generateKey()
        }
    }
