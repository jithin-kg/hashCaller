package com.nibble.hashcaller.repository.search

import android.annotation.SuppressLint
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.search.ISearchService
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.utils.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception

class SearchNetworkRepository(private val tokenManager: TokenManager){

    private var retrofitService:ISearchService?  = RetrofitClient.createaService(ISearchService::class.java)
    @SuppressLint("LongLogTag")

    suspend fun search(phoneNum:String): Response<SerachRes>?  = withContext(Dispatchers.IO){
        var result : Response<SerachRes>? = null
        try {

            val token = tokenManager.getDecryptedToken()
            result =  retrofitService?.search(SearchDTO(phoneNum), token)
            Log.d(TAG, "search: $result")
        }catch (e:Exception){

            Log.d(TAG, "search:exception $e")
        }

        return@withContext result
    }

    suspend fun incrementTotalSpamCount()  = withContext(Dispatchers.IO) {
       try {
           val token = tokenManager.getDecryptedToken()

           retrofitService!!.incrementTotalSpamCount(token)
       }catch (e:Exception){
           Log.d(TAG, "incrementTotalSpamCount: ")
       }
    }

    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}