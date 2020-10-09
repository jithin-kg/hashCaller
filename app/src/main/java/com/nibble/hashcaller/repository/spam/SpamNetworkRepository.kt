package com.nibble.hashcaller.repository.spam

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.search.ISearchService
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.utils.auth.TokenManager
import retrofit2.Response

class SpamNetworkRepository(private val context: Context){

    private var retrofitService:ISpamService? = null
    @SuppressLint("LongLogTag")

    suspend fun report(callerInfo: ReportedUserDTo): Response<NetWorkResponse>? {
        retrofitService = RetrofitClient.createaService(ISpamService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

//        val response = retrofitService?.search(SearchDTO(phoneNum), token)
//        Log.d(TAG, "signup: ${response?.body()?.message}")


//        return response
         return retrofitService?.report(callerInfo, token)
    }
    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}