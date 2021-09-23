package com.hashcaller.app.view.ui.splashactivity

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.network.user.Result
import com.hashcaller.app.network.user.SingupResponse
import com.hashcaller.app.repository.user.UserInfoDTO
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception

class SplashActivityViewModel(
    val repository: SplashActivityRepository?

) :ViewModel(){

    @SuppressLint("LongLogTag")
//    fun upload(userInfo: UserInfoDTO)= liveData(){
//
//
//
//        Log.d(TAG, "upload: inside ")
//        emit(Resource.loading(null))
//        /**
//         * saving user info in local db
//         */
//        /**
//         * saving user info in local db
//         */
////        saveUserInfoInLocalDb(userInfo)
////            userNetworkRepository.signup(userInfo)
//        try {
//            Log.d(UserInfoViewModel.TAG, "upload: try")
//            var result:String? = ""
////            val response = repository!!.signup(userInfo)
//
//            val success = response?.isSuccessful?:false
//            if(success){
//                Log.d(TAG, "signup: ${response?.body()}")
//                result = response?.body()?.message
//
//                Log.d(TAG, "signup: $result")
//            }else{
//                Log.d(TAG, "signup: failure")
//            }
////            Log.d(TAG, "upload: response is $result.")
//            emit(Resource.success(response))
//        }catch (e: Exception){
//            Log.d(TAG, "upload: exception $e")
//            emit(Resource.error(null, e.message));
//        }
//
//    }
    //todo mvoe saving to main activity after performing the upload, that may be leading to memory leak
    private suspend fun saveUserInfoInLocalDb(userInfo: UserInfoDTO) {
//        this.repository!!.saveUserInfoInLocalDb(userInfo)
    }

    /**
     * called from splash activity
     */
     fun getUserInfo():UserInfo? {
        var res :UserInfo? = null
            viewModelScope.launch {
                 res = repository?.getuserInfo()
            }
        return res
    }
   @SuppressLint("LongLogTag")
   fun getUserInfoFromServer(): LiveData<SingupResponse?> = liveData {
    var response:Response<SingupResponse>? = null
       viewModelScope.launch {
           try {

//                response =  repository?.getUserInfoFromServer()



           }catch (e:Exception){
               Log.d(TAG, "getUserInfoFromServer:exception $e")
           }
       }.join()
       if(response!=null){
           if(response?.isSuccessful!!){
               if(response!!.body()!=null){
                   emit(response!!.body())
               }

           }
       }

    }

    fun saveUserInfo(result: Result) :LiveData<Int> = liveData {
        repository?.saveUserInfoInLocalDb(UserInfo(null, result.firstName?:"",
            result.lastName?:"", "sample ", "sample","sample", result.image?:""))

        emit(OPERATION_COMPLETED)
    }


    companion object{
        const val TAG = "__SplashActivityViewModel"

    }

}