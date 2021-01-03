package com.nibble.hashcaller.repository.cipher

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.network.user.ResponseCipher
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.utils.auth.TokenManager
import retrofit2.Response

class CipherNetworkRepository (private val context: Context){
    private var retrofitService: IuserService? = null

    @SuppressLint("LongLogTag")
    suspend fun getCipher(): Response<ResponseCipher>? {
        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        val response = retrofitService?.getCipher(token)
        Log.d(TAG, "signup: ${response?.body()?.cipher}")
        return response
    }
    companion object{
        private const val TAG = "__CipherNetworkRepository"
    }
}