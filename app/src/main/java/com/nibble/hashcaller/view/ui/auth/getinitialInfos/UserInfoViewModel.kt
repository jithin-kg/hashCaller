package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.network.user.GetUserInfoResponse
import com.nibble.hashcaller.network.user.SingupResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.lang.Exception

class UserInfoViewModel(
    private val userNetworkRepository: UserNetworkRepository
) :ViewModel(){
    var userInfo = userNetworkRepository.getUserInfo()
//    val userInfo  = userNetworkRepository.getUserInfo()
    fun upload(userInfo: UserInfoDTO, body: MultipartBody.Part?):LiveData<SingupResponse> = liveData{



        Log.d(TAG, "upload: inside ")
    /**
         * saving user info in local db
         */
        /**
         * saving user info in local db
         */
//        saveUserInfoInLocalDb(userInfo)
//            userNetworkRepository.signup(userInfo)
        try {
            Log.d(TAG, "upload: try")
            var result:String? = ""
            val response = userNetworkRepository.signup(userInfo, body)
            Log.d(TAG, "upload: response is $response")
           if(response?.isSuccessful){
               response.body()?.let { emit(it) }
           }


//        if(success){
//            Log.d(TAG, "signup: ${response?.body()}")
//             result = response?.body()?.message
//
//            Log.d(TAG, "signup: $result")
//        }else{
//            Log.d(TAG, "signup: failure")
//        }
//            Log.d(TAG, "upload: response is $result.")
//            emit(Resource.success(response))
        }catch (e:Exception){
            Log.d(TAG, "upload: exception $e")
//            emit(Resource.error(null, e.message));
        }



    }

    fun saveUserInfoInLocalDb(singupResponse: SingupResponse):LiveData<Int> = liveData {
        viewModelScope.launch {
            val user = UserInfo(null)
            val result = singupResponse.result
//            user.email = result.email
            user.firstname = result.firstName
            user.lastName = result.lastName
            user.phoneNumber = "2"
            user.photoURI = result.image

            userNetworkRepository.saveUserInfoInLocalDb(user)
        }.join()
        emit(OPERATION_COMPLETED)
//        userNetworkRepository.saveUserInfoInLocalDb(userInfo = )
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

    fun getUserInfoFromServer(): LiveData<SingupResponse> = liveData {
        viewModelScope.launch {
            try {
                val response =  userNetworkRepository.getUserInfoFromServer()
                Log.d(TAG, "getUserInfoFromServer: response: $response")
                Log.d(TAG, "getUserInfoFromServer: responsebody: ${response.body()}")
                if(response.isSuccessful){
                    if(response.body()!=null)
                    if(!response.body()?.result?.firstName.isNullOrEmpty()){
                        emit(response.body()!!)
                    }
                }

            }catch (e:Exception){
                Log.d(TAG, "getUserInfoFromServer: exception $e")

            }
        }
    }

    fun insertUserInfo(userInfo: GetUserInfoResponse?) = viewModelScope.launch{
        val user = UserInfo(null, userInfo!!.firstName, userInfo!!.lastName, "", "", "")
        userNetworkRepository.insertNewUserIntoDb(user)
    }

    fun compresSAndPrepareForUpload(imgFile: File?, context: GetInitialUserInfoActivity) :LiveData<MultipartBody.Part> = liveData {
        val compressedImageFile: File = Compressor.compress(
            context,
            imgFile!!
        ) {
                                    resolution(48, 48)
//                                    quality(80)
//                                    format(Bitmap.CompressFormat.WEBP)
            size(4000) // 4 kb
        }
        val requestFile: RequestBody =
            compressedImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
       val  body:MultipartBody.Part =  MultipartBody.Part.createFormData(
            "image",
            compressedImageFile.name,
            requestFile
        );

//        val requestFile: okhttp3.RequestBody? =
//            imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//        body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }
        emit(body)

    }
}