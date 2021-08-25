package com.hashcaller.app.repository.cipher

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.network.user.ResponseCipher
import com.hashcaller.app.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import retrofit2.Response

class CipherNetworkRepository (private val context: Context){
    private var retrofitService: IuserService? = null

    @SuppressLint("LongLogTag")
    suspend fun getCipher(): Response<ResponseCipher>? {
        retrofitService = RetrofitClient.createaService(IuserService::class.java)

        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
//        val tokenManager = TokenManager(sp)
//        val token = tokenManager.getToken()

//        val response = retrofitService?.getCipher(token)
        Log.d(TAG, "getCipher: ")
//        Log.d(TAG, "signup: ${response?.body()?.cipher}")
//        return response
        return null
    }
    companion object{
        private const val TAG = "__CipherNetworkRepository"
    }
}