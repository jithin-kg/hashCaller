package com.nibble.hashcaller.view.ui.splashactivity

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class SplashActivityViewModel(
    val repository: SplashActivityRepository?

) :ViewModel(){

    @SuppressLint("LongLogTag")
    fun upload(userInfo: UserInfoDTO)= liveData(){



        Log.d(TAG, "upload: inside ")
        emit(Resource.loading(null))
        /**
         * saving user info in local db
         */
        /**
         * saving user info in local db
         */
//        saveUserInfoInLocalDb(userInfo)
//            userNetworkRepository.signup(userInfo)
        try {
            Log.d(UserInfoViewModel.TAG, "upload: try")
            var result:String? = ""
            val response = repository!!.signup(userInfo)

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
        }catch (e: Exception){
            Log.d(TAG, "upload: exception $e")
            emit(Resource.error(null, e.message));
        }

    }
    //todo mvoe saving to main activity after performing the upload, that may be leading to memory leak
    private suspend fun saveUserInfoInLocalDb(userInfo: UserInfoDTO) {
        this.repository!!.saveUserInfoInLocalDb(userInfo)
    }


    companion object{
        const val TAG = "__SplashActivityViewModel"

    }

}