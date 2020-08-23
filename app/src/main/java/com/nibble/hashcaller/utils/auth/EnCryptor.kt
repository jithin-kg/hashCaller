package com.nibble.hashcaller.utils.auth

import android.util.Base64
import android.util.Log
import java.io.IOException
import java.security.*
import java.util.*
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
        Log.d("__IV", "iv in encryptor is :${Base64.encodeToString(iv, Base64.DEFAULT)} ")
        Log.d("__IV", "token in encryptor is :$textToEncrypt ")
//        we are creating a intitialization vector with fixed length of 10
//        iv = Arrays.copyOf(iv, 12)

        Log.d(TAG, "encryptText: intitialization vector length is ${iv?.size}")

        //        Log.d("__Enc", "encryptText: "+ Arrays.toString(cipher.doFinal(textToEncrypt.getBytes("UTF-8"))));
//            Enc.INSTANCE.setEncryption(cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
        encryption = cipher?.doFinal(textToEncrypt.toByteArray(charset("UTF-8")))

        val stringEncToken = String(this!!.encryption!!,charset("UTF-8"))
        Log.d(TAG, "encryptText encrypted byte array is : $stringEncToken ")

        EncryptorObject.encryption = encryption
        EncryptorObject.iv = iv


       val ivWithEncryptionByteArrays =  iv!! + encryption!!
//        return  EncryptorObject.encryption
//        val encodeTokenString = Base64.encodeToString(
//            encryption,
//            Base64.DEFAULT
//        )
//        Log.d(TAG, "encryptText: encod")
//        val encodedIv = Base64.encodeToString(
//            iv,
//            Base64.DEFAULT
//        )
//        val tokenWithIv = encodedIv + encodeTokenString
        return  ivWithEncryptionByteArrays
    }



}