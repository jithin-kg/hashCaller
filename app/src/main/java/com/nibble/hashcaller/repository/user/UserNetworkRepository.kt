package com.nibble.hashcaller.repository.user

import ContactRepository
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import retrofit2.Response

/**
 * Created by Jithin KG on 13,August,2020
 */
class UserNetworkRepository(
    private val context: Context,
    private val userInfoDAO: UserInfoDAO,
    private val senderInfoFromServerDAO: SMSSendersInfoFromServerDAO
){
    private var retrofitService:IuserService? = null
    
    suspend fun signup(userInfo:UserInfoDTO): Response<NetWorkResponse>? {
        retrofitService = RetrofitClient.createaService(IuserService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        val response = retrofitService?.signup(userInfo, token)
        Log.d(TAG, "signup: ${response?.body()?.message}")
        return response
    }

    suspend  fun saveUserInfoInLocalDb(userInfo: UserInfoDTO) {
        this.userInfoDAO.insert(UserInfo(null, userInfo.firstName,
        userInfo.lastName, userInfo.phoneNumber, userInfo.phoneNumber))
    }

    suspend fun getUserInfo(): UserInfo {
        return this.userInfoDAO.get()
    }

    suspend fun setContactsInHashMap() {

    }

    companion object{
        private const val TAG = "__UserNetworkRepository"
    }
}