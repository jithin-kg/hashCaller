package com.nibble.hashcaller.view.ui.sms

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.SpammerInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpammersInfoFromServer
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenManager

class SMScontainerRepository(val context: Context, val dao: SpammerInfoFromServerDAO) {

    private var retrofitService:ISpamService? = null

    suspend fun getSpammersStoredInLocalDB(): List<SpammersInfoFromServer> {
       val list =  dao.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums) {
         retrofitService = RetrofitClient.createaService(ISpamService::class.java)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        val response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
    }

    companion object{
        const val TAG = "__SMScontainerRepository"
    }

}