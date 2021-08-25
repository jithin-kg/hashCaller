package com.hashcaller.app.utils.auth

import android.util.Base64
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
    //todo add try catch here becase runtime errors
        val fullByteArray = encryptionIv!! + encryptedData!!;
        val fullString = Base64.encodeToString(fullByteArray, Base64.DEFAULT)


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