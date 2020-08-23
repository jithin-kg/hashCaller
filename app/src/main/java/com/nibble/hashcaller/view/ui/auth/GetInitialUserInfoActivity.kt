package com.nibble.hashcaller.view.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EncryptorObject
import com.nibble.hashcaller.view.ui.SplashActivity
import java.nio.charset.Charset

class GetInitialUserInfoActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_get_initial_user_info)


        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
        val stringTokenFromSharedPref = sharedPreferences.getString(SHARED_PREFERENCE_TOKEN_KEY, "")
        Log.d(TAG, "Token is ${stringTokenFromSharedPref}")
        val tokneByteOne = Base64.decode(stringTokenFromSharedPref, Base64.DEFAULT)
        Log.d(TAG, "onCreate: ${tokneByteOne.size}")

        val iv = tokneByteOne.copyOfRange(0, 12);

        val tokenByteElements = tokneByteOne.copyOfRange(12, (tokneByteOne!!.size ))


        val decryptor = Decryptor()
        var token = decryptor?.decryptData(
            SAMPLE_ALIAS,
            tokenByteElements,
            iv
        ).toString()


    }

    companion object{

        const val TAG = "__GetInitialUserInfoActivity"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
        private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
    }
}