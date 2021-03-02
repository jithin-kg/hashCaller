package com.nibble.hashcaller.view.ui.sms

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.sms.work.UnknownSMSsendersInfoResponse
import retrofit2.Response

class SMScontainerRepository(val context: Context, val dao: SMSSendersInfoFromServerDAO) {

    private var retrofitService:ISpamService? = null

    /**
     * @return all sms senders numbers list in the localDB which contains 
     * ____________________________________________________
     * contact_address | spammeReportCount | informationRecivedDate | name | type (business or general user) | 
     * -----------------------------------------------------
     * 
     * this is the table schema
     */
    suspend fun geSmsSendersStoredInLocalDB(): List<SMSSendersInfoFromServer> {
       val list =  dao.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownSMSsendersInfoResponse> {
         retrofitService = RetrofitClient.createaService(ISpamService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        val response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
        return response
    }

    @SuppressLint("LongLogTag")
    suspend fun deleteSmsThread(id: Long): Int {
        Log.d(TAG, "deleteSmsThread: threadid $id")
        var numRowsDeleted = 0
        var uri = Telephony.Sms.CONTENT_URI
        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        try {
           numRowsDeleted =  context.contentResolver.delete(uri, selection, selectionArgs)
        } catch (e: Exception) {
            Log.d(TAG, "deleteSmsThread: exception $e")
        }
        return numRowsDeleted
    }

    companion object{
        const val TAG = "__SMScontainerRepository"
    }

}