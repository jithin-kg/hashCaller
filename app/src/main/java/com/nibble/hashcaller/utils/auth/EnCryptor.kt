package com.nibble.hashcaller.utils.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.*
import javax.crypto.*

/**
 * Created by Jithin KG on 29,July,2020
 */
internal class EnCryptor {
    private lateinit var encryption: ByteArray
    private lateinit var iv: ByteArray

    @Throws(
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        SignatureException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun encryptText(alias: String, textToEncrypt: String): ByteArray {
        val cipher =
            Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            getSecretKey(alias)
        ) // here we call for getSecretKey to get secret eku
        iv = cipher.iv
        return cipher.doFinal(textToEncrypt.toByteArray(charset("UTF-8")))
            .also { encryption = it }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun getSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun getEncryption(): ByteArray {
        return encryption
    }

    fun getIv(): ByteArray {
        return iv
    }

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}