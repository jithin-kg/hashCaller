package com.nibble.hashcaller.repository.user

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.utils.auth.TokenManager
import kotlinx.coroutines.Deferred
import retrofit2.Response

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository (private val context:Context){
    private var retrofitService:IuserService? = null
    
    suspend fun signup(userInfo:UserInfoDTO): Response<NetWorkResponse>? {
        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        val response = retrofitService?.signup(userInfo, token)
        Log.d(TAG, "signup: ${response?.body()?.message}")

//        val success = response?.isSuccessful?:false
//
//        if(success){
//            Log.d(TAG, "signup: ${response?.body()}")
//            val result = response?.body()?.message
//
//            Log.d(TAG, "signup: $result")
//        }else{
//            Log.d(TAG, "signup: failure")
//        }
        return response
        
    }
    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}