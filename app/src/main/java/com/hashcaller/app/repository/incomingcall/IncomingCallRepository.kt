package com.hashcaller.app.repository.incomingcall

import android.util.Log
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.incomingcall.IIncomingCallService
import com.hashcaller.app.network.incomingcall.SuggestNameModel
import com.hashcaller.app.network.search.ISearchService
import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.repository.search.SearchDTO
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.auth.TokenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception

class IncomingCallRepository(private val tokenHelper: TokenHelper?) {

    companion object{
        private const val TAG = "--IncomingCallRepository"
    }

    private var retrofitService: IIncomingCallService?  = RetrofitClient.createaService(IIncomingCallService::class.java)


    suspend fun suggestName(suggestNameModel: SuggestNameModel): Response<SuggestNameModel.Response>?  = withContext(Dispatchers.IO){
        var result : Response<SuggestNameModel.Response>? = null
        try {
//            val token = tokenManager.getDecryptedToken()
            val token =  tokenHelper?.getToken()
            if(!token.isNullOrEmpty()){
                result =  retrofitService?.suggestName(suggestNameModel ,token)
            }

        }catch (e: Exception){
            Log.e(TAG, "suggestName: ",e)
        }

        return@withContext result
    }

    suspend fun upvote(suggestNameModel: SuggestNameModel): Response<SuggestNameModel.Response>?  = withContext(Dispatchers.IO){
        var result : Response<SuggestNameModel.Response>? = null
        try {
//            val token = tokenManager.getDecryptedToken()
            val token =  tokenHelper?.getToken()
            if(!token.isNullOrEmpty()){
                result =  retrofitService?.upVote(suggestNameModel ,token)
            }

        }catch (e: Exception){
            Log.e(TAG, "suggestName: ",e)
        }

        return@withContext result
    }


    suspend fun downVote(suggestNameModel: SuggestNameModel): Response<SuggestNameModel.Response>?  = withContext(Dispatchers.IO){
        var result : Response<SuggestNameModel.Response>? = null
        try {
//            val token = tokenManager.getDecryptedToken()
            val token =  tokenHelper?.getToken()
            if(!token.isNullOrEmpty()){
                result =  retrofitService?.downVote(suggestNameModel ,token)
            }

        }catch (e: Exception){
            Log.e(TAG, "suggestName: ",e)
        }

        return@withContext result
    }

}