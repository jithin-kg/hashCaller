package com.nibble.hashcaller.repository.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.network.user.SingupResponse
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.nibble.hashcaller.view.utils.imageProcess.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository(
    private val tokenManager: TokenManager,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: SMSSendersInfoFromServerDAO,
    private val imageCompressor: ImageCompressor
){
    private var retrofitService:IuserService = RetrofitClient.createaService(IuserService::class.java)
    
    suspend fun signup(userInfo: UserInfoDTO, imgMultipartBody: MultipartBody.Part?): Response<SingupResponse>   = withContext(Dispatchers.IO) {
//        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        val token = tokenManager.getToken()
        val firstName = createPartFromString(userInfo.firstName)
        val lastName = createPartFromString(userInfo.lastName)
        val hashedNum = createPartFromString(userInfo.hashedNum)
        val phoneNumber = createPartFromString(userInfo.phoneNumber)
        val countryCode = createPartFromString(userInfo.countryCode)
        val countryISO = createPartFromString(userInfo.countryISO)
        val response = retrofitService?.signup(firstName, lastName, hashedNum, phoneNumber, countryCode, countryISO, imgMultipartBody, token)

        if(response.isSuccessful){
            Log.d(TAG, "signup: success")
        }else{
            Log.d(TAG, "signup: failure")
        }
        Log.d(TAG, "signup:error body ${response?.errorBody()}")
//        Log.d(TAG, "signup: ${response?.body()?.message}")
        return@withContext response
    }

    fun createPartFromString(str:String): RequestBody {
        return str.toRequestBody(MultipartBody.FORM)
    }
    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfo)  = withContext(Dispatchers.IO){

        userInfoDAO.insert(userInfo)
    }

    /**
     * function to get user info from local db
     */
     fun getUserInfo(): LiveData<UserInfo> {
        return this.userInfoDAO.getUserInfoLiveData()
    }

    suspend fun setContactsInHashMap() {

    }

    suspend fun getUserInfoFromServer(): Response<SingupResponse> = withContext(Dispatchers.IO) {
        val token = tokenManager.getToken()
        return@withContext retrofitService.getUserInfo(token)
    }

    suspend fun insertNewUserIntoDb(userInfo: UserInfo)  = withContext(Dispatchers.IO) {
        userInfoDAO?.insert(user = userInfo)
    }

    suspend fun getCompressedImageBody(context: Context, imgFile: File): MultipartBody.Part  = withContext(Dispatchers.Default){
        return@withContext  imageCompressor.getCompressedImagePart(imgFile)

    }




    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}