package com.hashcaller.app.repository.incomingcall

import android.util.Log
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.incomingcall.IIncomingCallService
import com.hashcaller.app.network.incomingcall.SuggestNameModel
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception
import java.util.*

class IncomingCallRepository(
    private val tokenHelper: TokenHelper?,
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO,
    private val libCountryHelper: LibPhoneCodeHelper,
    private val countryCodeIso: String
) {

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
            return@withContext null
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
            return@withContext null
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
            return@withContext null
        }
        return@withContext result
    }

    suspend fun saveSugestionInDb(hashedNum: String, formatedNum: String, name: String) = withContext(Dispatchers.IO) {
        val res = callerInfoFromServerDAO.findByHash(hashedNum)
        if(res == null){
            //insert new record
            val info = CallersInfoFromServer( contactAddress = formatedNum, hashedNum = hashedNum, nameInPhoneBook = name,informationReceivedDate =  Date())
            callerInfoFromServerDAO.insert(listOf(info))
        }else {
            //update
            callerInfoFromServerDAO.updateBySuggestion(hashedNum, name)
        }

    }


}