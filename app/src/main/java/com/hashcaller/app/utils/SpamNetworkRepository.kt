package com.hashcaller.app.utils

import android.content.Context
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.contact.NetWorkResponse
import com.hashcaller.app.network.spam.ISpamService
import com.hashcaller.app.network.spam.ReportedUserDTo
import com.hashcaller.app.utils.auth.TokenManager
import retrofit2.Response

class SpamNetworkRepository(private val context: Context,private val dataStoreRepository: DataStoreRepository) {
    private val  retrofitService:ISpamService = RetrofitClient.createaService(ISpamService::class.java)

    suspend fun report(callerInfo: ReportedUserDTo): Response<NetWorkResponse>? {


        val tokenManager = TokenManager(dataStoreRepository )
        val token = tokenManager.getDecryptedToken()

        return retrofitService?.report(callerInfo, token)
    }
}