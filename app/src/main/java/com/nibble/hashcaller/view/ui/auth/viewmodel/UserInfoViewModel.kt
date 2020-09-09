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
            emit(Resource.loading(null))
//            userNetworkRepository.signup(userInfo)
        try {
            emit(Resource.success(userNetworkRepository.signup(userInfo)))
        }catch (e:Exception){
            Log.d(TAG, "upload: $e")
            emit(Resource.error(null, e.message));
        }



    }
    companion object{
        const val TAG = "__UserInfoViewModel"
    }
}