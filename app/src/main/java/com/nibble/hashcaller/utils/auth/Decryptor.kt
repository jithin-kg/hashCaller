package com.nibble.hashcaller.utils.auth

import android.util.Base64
import android.util.Log
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

public class Decryptor {
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val ANDROID_KEY_STORE = "AndroidKeyStore"

    private var keyStore: KeyStore? = null


    init {
        initKeyStore()
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class
    )
    private fun initKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore?.load(null)
    }

    @Throws(
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidAlgorithmParameterException::class
    )
    fun decryptData(
        alias: String?,
        encryptedData: ByteArray?,
        encryptionIv: ByteArray?
    ): String? {
        Log.d("__IV", "iv in decryptor is ${Base64.encodeToString(encryptionIv, Base64.DEFAULT).trim()}: ")
        Log.d("__IV", "token in decryptor is ${Base64.encodeToString(encryptedData, Base64.DEFAULT).trim()}: ")
        val fullByteArray = encryptionIv!! + encryptedData!!;
        val fullString = Base64.encodeToString(fullByteArray, Base64.DEFAULT)
        Log.d("__IV", "fullTokenString ${fullString}:  + lengtht is ${fullString.length}")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec =
            GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(alias!!), spec)
        cipher.doFinal(encryptedData)
        return String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        UnrecoverableEntryException::class,
        KeyStoreException::class
    )
    private fun getSecretKey(alias: String): SecretKey? {
        return (keyStore!!.getEntry(alias,
            null) as KeyStore.SecretKeyEntry).secretKey
    }

}