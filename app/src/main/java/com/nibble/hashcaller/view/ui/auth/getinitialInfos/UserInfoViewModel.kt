package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.network.NetworkResponseBase
import com.nibble.hashcaller.network.NetworkResponseBase.Companion.EVERYTHING_WENT_WELL
import com.nibble.hashcaller.network.NetworkResponseBase.Companion.SOMETHING_WRONG_HAPPEND
import com.nibble.hashcaller.network.user.GetUserInfoResponse
import com.nibble.hashcaller.network.user.Result
import com.nibble.hashcaller.network.user.SingupResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File
import java.lang.Exception

class UserInfoViewModel(
    private val userNetworkRepository: UserNetworkRepository,
    private val userHashedNumRepository: UserHasehdNumRepository
) :ViewModel(){
    var userInfo = userNetworkRepository.getUserInfo()
//    val userInfo  = userNetworkRepository.getUserInfo()


    fun upload(userInfo: UserInfoDTO,
              body: MultipartBody.Part?,
          context: Context
                ):LiveData<NetworkResponseBase<SingupResponse>>
            = liveData{


//    Secrets().managecipher(context.packageName, formattedPhoneNum)
    /**
         * saving user info in local db
         */
        /**
         * saving user info in local db
         */
//        saveUserInfoInLocalDb(userInfo)
//            userNetworkRepository.signup(userInfo)
        try {

           val hashedNumResultFromDb =  userHashedNumRepository.getHasehedNumOfuser()
            if(hashedNumResultFromDb!=null){
                var result:String? = ""
                val info = UserInfoDTO()
                info.firstName = userInfo.firstName
                info.lastName = userInfo.lastName
                info.hashedNum = hashedNumResultFromDb.hashedNumber
                info.phoneNumber = hashedNumResultFromDb.phoneNumber
                info.countryCode = userHashedNumRepository.getCoutryCode()
                info.countryISO = userHashedNumRepository.getCoutryISO()
                val response:Response<SingupResponse> = userNetworkRepository.signup(info, body)
                if(response?.isSuccessful){
                    response.body()?.let {
                        if(response.code() !in (400 .. 599)){
                            val genericResponse = NetworkResponseBase(it, EVERYTHING_WENT_WELL)
                            emit(genericResponse)
                        }
                    }
                }else {
                    if(response.code() in (400 .. 599)){
                        val genericRespnse = NetworkResponseBase(
                            SingupResponse(Result("", "", "")),SOMETHING_WRONG_HAPPEND )
                        emit(genericRespnse)
                    }
                }
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




//        val requestFile: okhttp3.RequestBody? =
//            imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//        body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }
        emit( userNetworkRepository.getCompressedImageBody(context, imgFile))

    }

    fun processImage(context: Context, selectedImageUri: Uri?, imagePickerHelper: ImagePickerHelper) {
        imagePickerHelper.processImage(context,selectedImageUri)

    }

    fun saveUserPhoneHash(context: Context, phoneNumber: String):LiveData<Int> = liveData{
        val formattedPhoneNum = formatPhoneNumber(phoneNumber)
        val hashedPhoneNum = Secrets().managecipher(context.packageName, formattedPhoneNum)
        userHashedNumRepository.saveUserPhoneHash(hashedPhoneNum, formattedPhoneNum)
        emit(OPERATION_COMPLETED)
    }
}