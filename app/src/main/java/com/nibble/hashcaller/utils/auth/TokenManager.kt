package com.nibble.hashcaller.utils.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.nibble.hashcaller.datastore.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.IOException
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Created by Jithin KG on 29,July,2020
 * This class healps to get encrypted token with iv from shared preferences
 * and return the  decrypted  token.
 */
class TokenManager(private val sharedPreferences:SharedPreferences,private val dataStoreRepository: DataStoreRepository) {

//    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"

    companion object{
        private const val TAG= "__TokenManager"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
        private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
        private const val IVArrStart = 0
        private const val IVArrEnd = 12
    }
     suspend fun  getToken(): String {
        var token = ""
//       applicationContext.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
//        val stringTokenFromSharedPref = sharedPreferences.getString(SHARED_PREFERENCE_TOKEN_KEY, "")
          val encodedToken =  dataStoreRepository.getToken()
        val tokneByteOne = Base64.decode(encodedToken, Base64.DEFAULT)

        val iv = tokneByteOne.copyOfRange(
            IVArrStart,
            IVArrEnd
        );

        val tokenByteElements = tokneByteOne.copyOfRange(IVArrEnd, (tokneByteOne!!.size ))


        try {
            val decryptor = Decryptor()
             token = decryptor?.decryptData(
                SAMPLE_ALIAS,
                tokenByteElements,
                iv
            ).toString()
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
        return token;


    }



}