package com.hashcaller.utils.auth

import android.util.Base64
import android.util.Log
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.view.ui.contacts.utils.SAMPLE_ALIAS

import kotlin.Exception

/**
 * Created by Jithin KG on 29,July,2020
 * This class healps to get encrypted token with iv from shared preferences
 * and return the  decrypted  token.
 */
class TokenManager(private val dataStoreRepository: DataStoreRepository) {

//    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        private const val TAG= "__TokenManager"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
        private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
        private const val IVArrStart = 0
        private const val IVArrEnd = 12
    }
     suspend fun  getDecryptedToken(): String {
        var token = ""
//       applicationContext.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
//        val stringTokenFromSharedPref = sharedPreferences.getString(SHARED_PREFERENCE_TOKEN_KEY, "")



        try {
            val encodedToken =  dataStoreRepository.getToken()
            val tokneByteOne = Base64.decode(encodedToken, Base64.DEFAULT)

            val iv = tokneByteOne.copyOfRange(
                IVArrStart,
                IVArrEnd
            );

            val tokenByteElements = tokneByteOne.copyOfRange(IVArrEnd, (tokneByteOne!!.size ))
            val decryptor = Decryptor()
             token = decryptor?.decryptData(
                SAMPLE_ALIAS,
                tokenByteElements,
                iv
            ).toString()
        } catch (e: Exception) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        }

        return token;


    }



}