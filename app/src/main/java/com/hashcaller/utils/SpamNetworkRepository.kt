package com.hashcaller.utils

import android.content.Context
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.network.RetrofitClient
import com.hashcaller.network.contact.NetWorkResponse
import com.hashcaller.network.spam.ISpamService
import com.hashcaller.network.spam.ReportedUserDTo
import com.hashcaller.utils.auth.TokenManager
import retrofit2.Response

class SpamNetworkRepository(private val context: Context,private val dataStoreRepository: DataStoreRepository) {
    private val  retrofitService:ISpamService = RetrofitClient.createaService(ISpamService::class.java)

    suspend fun report(callerInfo: ReportedUserDTo): Response<NetWorkResponse>? {


        val tokenManager = TokenManager(dataStoreRepository )
        val token = tokenManager.getDecryptedToken()

        return retrofitService?.report(callerInfo, token)
    }
}