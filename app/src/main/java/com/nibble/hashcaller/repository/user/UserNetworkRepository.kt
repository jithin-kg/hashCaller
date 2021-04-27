package com.nibble.hashcaller.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.user.GetUserInfoResponse
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.network.user.SingupResponse
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository(
    private val tokenManager: TokenManager,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: SMSSendersInfoFromServerDAO
){
    private var retrofitService:IuserService = RetrofitClient.createaService(IuserService::class.java)
    
    suspend fun signup(userInfo:UserInfoDTO): Response<SingupResponse>   = withContext(Dispatchers.IO) {
//        retrofitService = RetrofitClient.createaService(IuserService::class.java)

        val token = tokenManager.getToken()
        
        val response = retrofitService?.signup(userInfo, token)
        if(response.isSuccessful){
            Log.d(TAG, "signup: success")
        }else{
            Log.d(TAG, "signup: failure")
        }
        Log.d(TAG, "signup:error body ${response?.errorBody()}")
//        Log.d(TAG, "signup: ${response?.body()?.message}")
        return@withContext response
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

    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}