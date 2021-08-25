package com.hashcaller.app.view.ui.splashactivity

import android.util.Log
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.network.user.SingupResponse
import com.hashcaller.app.utils.auth.TokenManager
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception

/**
 * Created by Jithin KG on 13,August,2020
 */
class SplashActivityRepository(
    private val tokenManager: TokenManager,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: CallersInfoFromServer
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

    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfo)  = withContext(Dispatchers.IO) {
        userInfoDAO.insert(userInfo)
    }



    suspend fun setContactsInHashMap() {

    }

    suspend fun getuserInfo(): UserInfo?  = withContext(Dispatchers.IO) {
        return@withContext userInfoDAO?.getUser()
    }

    suspend fun getUserInfoFromServer(): Response<SingupResponse>?   = withContext(Dispatchers.IO){
        try {
            val token = tokenManager.getDecryptedToken()
//            val res =  retrofitService?.getUserInfo(token, hashedNum, formattedPhoneNum)
//            return@withContext res
        }catch (e:Exception){
            Log.d(TAG, "getUserInfoFromServer: $e")
        }
        return@withContext null

    }

    companion object {
        private const val TAG = "__UserNetworkRepository"
    }
}