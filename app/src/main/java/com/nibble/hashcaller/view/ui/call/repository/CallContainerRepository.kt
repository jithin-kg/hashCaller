package com.nibble.hashcaller.view.ui.call.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedItems
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.contacts.utils.isNumericOnlyString
import com.nibble.hashcaller.work.formatPhoneNumber
import com.nibble.hashcaller.work.replaceSpecialChars
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class CallContainerRepository(
    val context: Context,
    val dao: CallersInfoFromServerDAO,
    val mutedCallersDAO: IMutedCallersDAO?
) {

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
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val tokenManager = TokenManager(sp)
        val token = tokenManager.getToken()

        val response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
        return response
    }

    suspend fun getNameForAddress(number: String): CallersInfoFromServer? {
        val numWithoutSpecialChars = replaceSpecialChars(number)
        var numberForQuery =numWithoutSpecialChars
        if(isNumericOnlyString(numWithoutSpecialChars)){
            numberForQuery = formatPhoneNumber(numWithoutSpecialChars)
        }
        var result: CallersInfoFromServer? = null
        GlobalScope.launch {
            result= async { dao.find(numberForQuery) }.await()
        }.join()

        return result

    }

    @SuppressLint("LongLogTag")
    suspend fun deleteLogs() {

//        val queryString = "NUMBER=$number"
//        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, queryString, null);


//        smsDeletingStarted = true
//        var numRowsDeleted = 0
        var list: MutableSet<Long>  = mutableSetOf()
        list.addAll(getMarkedItems())
        try {
            for(id in list) {
                Log.d(TAG, "deleteSmsThread: threadid $id")
                var uri = CallLog.Calls.CONTENT_URI
                val selection = "${CallLog.Calls._ID} = ?"
                val selectionArgs = arrayOf(id.toString())
                try {
                    delay(800L)

                }
                finally {
                    context.contentResolver.delete(uri, selection, selectionArgs)
                    IndividualMarkedItemHandlerCall.clearlists()
                }
            }
        }catch (e: Exception) {
            Log.d(TAG, "deleteSmsThread: exception $e")
        }



    }

    /**
     * function to add contact address to mutedCallers
     */
    suspend fun muteContactAddress(address: String): String {
        mutedCallersDAO!!.insert(listOf(MutedCallers(address))).apply {
            return address
        }

    }

    @SuppressLint("LongLogTag")
    suspend fun unmuteByAddress(contactAdders: String): String {
        Log.d(TAG, "unmuteByAddress:${contactAdders}")
        mutedCallersDAO!!.delete(contactAdders).apply {
            return contactAdders
        }
    }

    /**
     * Function to check whether a number is muted or not,
     * if contactaddress contains in db then it is  muted
     */
    suspend fun isMmuted(address: String): Boolean {
        var isMutedNum = false
        mutedCallersDAO!!.find(address).apply {
            if(this!=null){
                isMutedNum = true
            }
            return isMutedNum
        }
    }

    companion object{
        const val TAG = "__CallContainerRepository"
    }

}