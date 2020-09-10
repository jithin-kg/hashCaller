package com.nibble.hashcaller.view.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import androidx.lifecycle.liveData
import com.nibble.hashcaller.network.user.Resource
import kotlinx.coroutines.*
import java.lang.Exception

class UserInfoViewModel(private val userNetworkRepository: UserNetworkRepository) :ViewModel(){


    fun upload(userInfo: UserInfoDTO)= liveData(Dispatchers.IO){
        Log.d(TAG, "upload: inside ")
            emit(Resource.loading(null))
//            userNetworkRepository.signup(userInfo)
        try {
            Log.d(TAG, "upload: try")
            var result:String? = ""
            val response = userNetworkRepository.signup(userInfo)

            val success = response?.isSuccessful?:false
        if(success){
            Log.d(TAG, "signup: ${response?.body()}")
             result = response?.body()?.message

            Log.d(TAG, "signup: $result")
        }else{
            Log.d(TAG, "signup: failure")
        }
//            Log.d(TAG, "upload: response is $result.")
            emit(Resource.success(response))
        }catch (e:Exception){
            Log.d(TAG, "upload: exception $e")
            emit(Resource.error(null, e.message));
        }



    }
    companion object{
        const val TAG = "__UserInfoViewModel"
    }
}