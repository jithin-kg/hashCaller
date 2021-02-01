package com.nibble.hashcaller.repository.search

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.search.ISearchService
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.utils.auth.TokenManager
import retrofit2.Response

class SearchNetworkRepository(private val context: Context){

    private var retrofitService:ISearchService? = null
    @SuppressLint("LongLogTag")

    suspend fun search(phoneNum:String): Response<SerachRes>? {
        retrofitService = RetrofitClient.createaService(ISearchService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

//        val response = retrofitService?.search(SearchDTO(phoneNum), token)
//        Log.d(TAG, "signup: ${response?.body()?.message}")


//        return response
         return retrofitService?.search(SearchDTO(phoneNum), token)
    }

    suspend fun incrementTotalSpamCount() {
        retrofitService = RetrofitClient.createaService(ISearchService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        retrofitService!!.incrementTotalSpamCount(token)
    }

    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}