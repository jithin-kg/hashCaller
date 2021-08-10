package com.hashcaller.utils.auth

import java.io.IOException
import java.security.*
import javax.crypto.*

/**
 * Created by Jithin KG on 30,July,2020
 */
class EnCryptor {
    private val TRANSFORMATION = "AES/GCM/NoPadding"

    private val TAG = "__EnCryptor"
    private var encryption: ByteArray? = null
    private var iv: ByteArray? = null
    private var cipher: Cipher? = null
    private val UTF8 = "UTF-8"
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
    fun encryptText(alias: String?, textToEncrypt: String): ByteArray {
        cipher = Cipher.getInstance(TRANSFORMATION)
        cipher?.init(
            Cipher.ENCRYPT_MODE,
            KeyStoreManager.getSecretKeyForEncryption(alias!!)
        ) // here we call for getSecretKey to get secret key

        iv = cipher?.iv


        encryption = cipher?.doFinal(textToEncrypt.toByteArray(charset(UTF8)))




        EncryptorObject.encryption = encryption
        EncryptorObject.iv = iv


       val ivWithEncryptionByteArrays =  iv!! + encryption!!

        return  ivWithEncryptionByteArrays
    }



}