package com.nibble.hashcaller.repository.search

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.search.ISearchService
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception

class SearchNetworkRepository(private val context: Context, private val dataStoreRepository: DataStoreRepository ){

    private var retrofitService:ISearchService? = null
    @SuppressLint("LongLogTag")

    suspend fun search(phoneNum:String): Response<SerachRes>?  = withContext(Dispatchers.IO){
        var result : Response<SerachRes>? = null
        try {
            retrofitService = RetrofitClient.createaService(ISearchService::class.java)
            val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
            val tokenManager = TokenManager(sp, dataStoreRepository)
            val token = tokenManager.getToken()
            result =  retrofitService?.search(SearchDTO(phoneNum), token)
        }catch (e:Exception){

            Log.d(TAG, "search:exception $e")
        }

        return@withContext result
    }

    suspend fun incrementTotalSpamCount()  = withContext(Dispatchers.IO) {
        retrofitService = RetrofitClient.createaService(ISearchService::class.java)
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val tokenManager = TokenManager(sp, dataStoreRepository)
        val token = tokenManager.getToken()

        retrofitService!!.incrementTotalSpamCount(token)
    }

    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}