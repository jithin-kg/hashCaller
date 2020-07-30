package com.nibble.hashcaller.utils.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Created by Jithin KG on 29,July,2020
 */
class TokenManager(private val applicationContext: Context) {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var encryptor: EnCryptor
    private lateinit var decryptor: Decryptor
    private val SAMPLE_ALIAS = "MYALIAS"


    fun getTokenFromSharedPreferences(){
        initCrypto()
        sharedPreferences = applicationContext.getSharedPreferences(
            "TOKEN",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()

    }
    private fun initCrypto() {
        encryptor = EnCryptor()
        try {
            decryptor = Decryptor()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun getToken(view: View?): String {
        var decryptedToken = ""
        try {

//            decryptedToken = decryptor
//                .decryptData(
//                    SAMPLE_ALIAS,
//                    encryptor.getEncryption(),
//                    encryptor.getIv()
//                )


        } catch (e: UnrecoverableEntryException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: KeyStoreException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: NoSuchProviderException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: IOException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
        return decryptedToken
    }
    companion object{
        private const val TAG= "__TokenManager"
    }
}