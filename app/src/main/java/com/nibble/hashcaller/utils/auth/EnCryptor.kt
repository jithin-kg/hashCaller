package com.nibble.hashcaller.utils.auth

import java.io.IOException
import java.security.*
import javax.crypto.*

/**
 * Created by Jithin KG on 30,July,2020
 */
class EnCryptor {
    private val TRANSFORMATION = "AES/GCM/NoPadding"


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
    fun encryptText(alias: String?, textToEncrypt: String): ByteArray? {
        cipher = Cipher.getInstance(TRANSFORMATION)
        cipher?.init(
            Cipher.ENCRYPT_MODE,
            KeyStoreManager.getSecretKeyForEncryption(alias!!)
        ) // here we call for getSecretKey to get secret key
        iv = cipher?.iv
        //        Log.d("__Enc", "encryptText: "+ Arrays.toString(cipher.doFinal(textToEncrypt.getBytes("UTF-8"))));
//            Enc.INSTANCE.setEncryption(cipher.doFinal(textToEncrypt.getBytes("UTF-8")));
        encryption = cipher?.doFinal(textToEncrypt.toByteArray(charset("UTF-8")))

        EncryptorObject.encryption = encryption
        EncryptorObject.iv = iv

        return  EncryptorObject.encryption
    }



}