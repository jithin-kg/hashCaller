package com.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.Secrets
import com.hashcaller.datastore.DataStoreViewmodel
import com.hashcaller.datastore.PreferencesKeys
import com.hashcaller.network.user.GetUserDataResponse
import com.hashcaller.network.user.GetUserInfoResponse
import com.hashcaller.network.user.SingupResponse
import com.hashcaller.repository.user.UserInfoDTO
import com.hashcaller.repository.user.UserNetworkRepository
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHashedNumber
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class UserInfoViewModel(
    private val userNetworkRepository: UserNetworkRepository,
    private val userHashedNumRepository: UserHasehdNumRepository
) :ViewModel(){
    var userInfoLivedata = userNetworkRepository.getUserInfoLiveData()
//    val userInfo  = userNetworkRepository.getUserInfo()




    fun saveUserInfoInLocalDb(
        singupResponse: SingupResponse,
        dataStoreViewmodel: DataStoreViewmodel
    ):LiveData<Int> = liveData {
        try {
            val user = UserInfo(null)
            val result = singupResponse.data
//            user.email = result.email
            user.firstname = result.firstName
            user.lastName = result.lastName
            user.phoneNumber = "2"
            user.photoURI = result.image?:""
            userNetworkRepository.saveUserInfoInLocalDb(user)
            dataStoreViewmodel.setBoolean(PreferencesKeys.USER_INFO_AVIALABLE_IN_DB, true)
            emit(OPERATION_COMPLETED)


        }catch (e:Exception){

            Log.d(TAG, "saveUserInfoInLocalDb: $e")
        }
           
//        userNetworkRepository.saveUserInfoInLocalDb(userInfo = )
    }

    fun setContactsHashMap() = viewModelScope.launch {
//            contactsRepository.setContactsMetaInfoHashMap()
    }

    fun getUserInfo():UserInfo? {
        var res :UserInfo? = null

        return res
    }

    fun getUserInfoFromDb():LiveData<UserInfo?> = liveData{
        emit(userNetworkRepository?.getUserInfo())
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

    fun getUserInfoFromServer( phoneNumber: String?, context: Context): LiveData<Response<SingupResponse>> = liveData {
            try {
                val formattedPhoneNum = formatPhoneNumber(phoneNumber!!)
                var hashedNum:String? = Secrets().managecipher(context.packageName, formattedPhoneNum)
//                hashedNum = hashUsingArgon(hashedNum)
                hashedNum?.let {
                    userNetworkRepository.getUserInfoFromServer( it, formattedPhoneNum)?.let { it1 ->
                        emit(
                            it1
                        )
                    }
//                    //todo update with status codes, in all api,
//                    Log.d(TAG, "getUserInfoFromServer: response: $response")
//                    Log.d(TAG, "getUserInfoFromServer: responsebody: ${response?.body()}")
////                    response?.let {
//                        if(response.code() == StatusCodes.FORBIDDEN){
//                            context.toast("Your account is blocked for violating our terms.", Toast.LENGTH_LONG)
//                        }else if(response?.isSuccessful && response.code() == StatusCodes.STATUS_OK){
//
//                            if(response.body()!=null)
//                                emit(response.body()!!)
////                    if(!response.body()?.result?.firstName.isNullOrEmpty()){
////                        emit(response.body()!!)
////                    }
//                        }else if(response.code() in 500..599){
//
//                        }
//                    }
                }



            }catch (e:Exception){
                Log.d(TAG, "getUserInfoFromServer: exception $e")

            }
    }

    fun insertUserInfo(userInfo: GetUserInfoResponse?) = viewModelScope.launch{
        val user = UserInfo(null, userInfo!!.firstName, userInfo!!.lastName, "", "", "")
        userNetworkRepository.insertNewUserIntoDb(user)
    }

    /**
     * function to compress image
     */
    fun compresSAndPrepareForUpload(imgFile: File?, context: Context) :LiveData<MultipartBody.Part?> = liveData {

        if(imgFile==null){
            emit(null)
        }else{
            emit( userNetworkRepository.getCompressedImageBody(context, imgFile))

        }

//        val requestFile: okhttp3.RequestBody? =
//            imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//        body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }

    }
    fun processImage(context: Context, selectedImageUri: Uri?, imagePickerHelper: ImagePickerHelper) {
        imagePickerHelper.processImage(context,selectedImageUri)

    }

    fun saveUserPhoneHash(context: Context, phoneNumber: String):LiveData<Int> = liveData{
        val formattedPhoneNum = formatPhoneNumber(phoneNumber)
        var hashedPhoneNum:String? = Secrets().managecipher(context.packageName, formattedPhoneNum)
//        hashedPhoneNum = hashUsingArgon(hashedPhoneNum)
        hashedPhoneNum?.let {
            userHashedNumRepository.saveUserPhoneHash(it, formattedPhoneNum) }
        emit(OPERATION_COMPLETED)

    }

    fun updateUserInfoInServer(userInfo: UserInfoDTO, imgMultiPart: MultipartBody.Part?):LiveData<Response<SingupResponse>> = liveData{
            try {
                val hashedNumResultFromDb =  userHashedNumRepository.getHasehedNumOfuser()
                if(hashedNumResultFromDb!=null){
                    val info = gePreparedPhonenum(userInfo, hashedNumResultFromDb)
                    val response:Response<SingupResponse>? = userNetworkRepository.updateUserInfoInServer(info, imgMultiPart)
                    response?.let {
                        emit(it)
//                        emit(getGenericResponse(response))
                    }

                }
            }catch (e:Exception){
                Log.d(TAG, "updateUserInfo: exception $e")
            }
    }

    fun upload(userInfo: UserInfoDTO,
               body: MultipartBody.Part?,
               context: Context
    ):LiveData<Response<SingupResponse>>
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
                val info = gePreparedPhonenum(userInfo, hashedNumResultFromDb)
                val response:Response<SingupResponse>? = userNetworkRepository.signup(info, body)
                response?.let {
                    emit(response)
                }
            }

        }catch (e:Exception){
            Log.d(TAG, "upload: exception $e")
//            emit(Resource.error(null, e.message));
        }

    }

//    private fun getGenericResponse(response: Response<SingupResponse>): NetworkResponseBase<SingupResponse> {
//        if(response?.isSuccessful){
//            response.body()?.let {
//                if(response.code() !in (400 .. 599)){
//                    return NetworkResponseBase(it, EVERYTHING_WENT_WELL)
//                }
//            }
//        }else {
//            if(response.code() in (400 .. 599)){
//                return NetworkResponseBase(
//                    SingupResponse(Result("", "", "","")),SOMETHING_WRONG_HAPPEND )
//            }
//        }
//        return NetworkResponseBase(
//            SingupResponse(Result("", "", "", "")),SOMETHING_WRONG_HAPPEND )
//    }

    private fun gePreparedPhonenum(userInfo: UserInfoDTO, hashedNumResultFromDb: UserHashedNumber): UserInfoDTO {
        val info = UserInfoDTO()
        info.firstName = userInfo.firstName
        info.lastName = userInfo.lastName
        info.hashedNum = hashedNumResultFromDb.hashedNumber
        info.phoneNumber = hashedNumResultFromDb.phoneNumber
        info.countryCode = userHashedNumRepository.getCoutryCode()
        info.countryISO = userHashedNumRepository.getCoutryISO()
        return info
    }

    fun updateUserInfoInDb(
        firstName: String?,
        lastName: String?,
        imgeFromServer: String?
    ) : LiveData<Int> = liveData{

        try {
            if(!firstName.isNullOrEmpty()){
                val user = UserInfo(null)
                userNetworkRepository.updateUserInfoInDb(firstName, lastName?:"", imgeFromServer?:"")
                emit(OPERATION_COMPLETED)
            }
        }catch (e:Exception){
            Log.d(TAG, "updateUserInfoInDb: $e")
        }

    }

    fun requestForUserInfoStoredInServer(email:String) = viewModelScope.launch {
        userNetworkRepository.requestForUserInfoInserver(email)
    }

    fun getUserDataInHashcaller():LiveData<Response<GetUserDataResponse>?> = liveData {
        withContext(Dispatchers.IO){
            //check if user data available in storage,if user data not in db get from server
            withContext(Dispatchers.Main){
              val  res =   userNetworkRepository.getMyData()
               withContext(Dispatchers.Main){
                   emit(res)
               }
            }
        }
    }

    fun saveFile(res: GetUserDataResponse?, fos: FileOutputStream) :LiveData<Int> = liveData {
        userNetworkRepository.saveFile(res, fos)
        emit(OPERATION_COMPLETED)
    }
}