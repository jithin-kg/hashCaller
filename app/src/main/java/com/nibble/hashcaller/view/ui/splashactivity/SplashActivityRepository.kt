package com.nibble.hashcaller.view.ui.splashactivity

import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.network.user.SingupResponse
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import retrofit2.Response
import java.lang.Exception

/**
 * Created by Jithin KG on 13,August,2020
 */
class SplashActivityRepository(
    private val tokenManager: TokenManager,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: SMSSendersInfoFromServerDAO
){
    private var retrofitService:IuserService = RetrofitClient.createaService(IuserService::class.java)
    
//    suspend fun signup(userInfo: UserInfoDTO): Response<NetWorkResponse>? {
//        retrofitService = RetrofitClient.createaService(IuserService::class.java)
//
//        val token = tokenManager.getToken()
//
//        val response = retrofitService?.signup(userInfo, token)
////        Log.d(TAG, "signup: ${response?.body()?.message}")
//        return response
//    }

    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfo) {
        this.userInfoDAO.insert(userInfo)
    }



    suspend fun setContactsInHashMap() {

    }

    suspend fun getuserInfo(): UserInfo? {
        return userInfoDAO?.getUser()
    }

    suspend fun getUserInfoFromServer(): Response<SingupResponse>? {
        try {
            val token = tokenManager.getToken()
            val res =  retrofitService?.getUserInfo(token)
            return res
        }catch (e:Exception){
            Log.d(TAG, "getUserInfoFromServer: $e")
        }
        return null

    }

    companion object {
        private const val TAG = "__UserNetworkRepository"
    }
}