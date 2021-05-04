package com.nibble.hashcaller.utils

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import retrofit2.Response

class SpamNetworkRepository(private val context: Context,private val dataStoreRepository: DataStoreRepository) {
    private val  retrofitService:ISpamService = RetrofitClient.createaService(ISpamService::class.java)

    suspend fun report(callerInfo: ReportedUserDTo): Response<NetWorkResponse>? {


        val tokenManager = TokenManager(dataStoreRepository )
        val token = tokenManager.getDecryptedToken()

        return retrofitService?.report(callerInfo, token)
    }
}