package com.nibble.hashcaller.view.ui.call.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
import com.nibble.hashcaller.view.ui.sms.work.UnknownSMSsendersInfoResponse
import retrofit2.Response

class CallContainerRepository(val context: Context, val dao: CallersInfoFromServerDAO) {

    private var retrofitService:ICallService? = null

    /**
     * @return all sms senders numbers list in the localDB which contains 
     * ____________________________________________________
     * contact_address | spammeReportCount | informationRecivedDate | name | type (business or general user) | 
     * -----------------------------------------------------
     * 
     * this is the table schema
     */
    suspend fun geSmsSendersStoredInLocalDB(): List<CallersInfoFromServer> {
       val list =  dao.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownCallersInfoResponse> {
         retrofitService = RetrofitClient.createaService(ICallService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        val response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
        return response
    }

    companion object{
        const val TAG = "__CallContainerRepository"
    }

}