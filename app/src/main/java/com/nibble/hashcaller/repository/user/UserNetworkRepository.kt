package com.nibble.hashcaller.repository.user

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.utils.auth.TokenManager

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository (private val context:Context){
    private var retrofitService:IuserService? = null
    
    suspend fun signup(userInfo:UserInfoDTO): String? {
        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        val response = retrofitService?.signup(userInfo, token)
        
        val success = response?.isSuccessful?:false
        
        if(success){
            Log.d(TAG, "signup: ${response?.body()}")
            val result = response?.body()?.message

            Log.d(TAG, "signup: $result")
        }else{
            Log.d(TAG, "signup: failure")
        }
        return response?.body()?.message
        
    }
    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}