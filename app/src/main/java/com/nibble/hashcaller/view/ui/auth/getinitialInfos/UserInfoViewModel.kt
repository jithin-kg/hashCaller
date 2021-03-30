package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import ContactRepository
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.utils.ContactRepositoryTwo
import kotlinx.coroutines.*
import java.lang.Exception

class UserInfoViewModel(
    private val userNetworkRepository: UserNetworkRepository
) :ViewModel(){
    var userInfo : MutableLiveData<UserInfo> = MutableLiveData()

    fun upload(userInfo: UserInfoDTO)= liveData(Dispatchers.IO){



        Log.d(TAG, "upload: inside ")
            emit(Resource.loading(null))
        /**
         * saving user info in local db
         */
        saveUserInfoInLocalDb(userInfo)
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

    private suspend fun saveUserInfoInLocalDb(userInfo: UserInfoDTO) {
        this.userNetworkRepository.saveUserInfoInLocalDb(userInfo)
    }

    fun getUserInfo() = viewModelScope.launch {
       val result  = userNetworkRepository.getUserInfo()
        userInfo.value = result
        Log.d(TAG, "getUserInfo: $result")
    }

    fun setContactsHashMap() = viewModelScope.launch {
//            contactsRepository.setContactsMetaInfoHashMap()
    }

    companion object{
        const val TAG = "__UserInfoViewModel"
    }

    /**
     * important to call this to prevent memory leak
     */
    override fun onCleared() {
        super.onCleared()
    }
}