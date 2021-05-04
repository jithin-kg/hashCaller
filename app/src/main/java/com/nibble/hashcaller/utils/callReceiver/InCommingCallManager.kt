package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import retrofit2.Response

/**
 * Created by Jithin KG on 20,July,2020
 */
class InCommingCallManager(
    private val context: Context,
    phoneNumber: String,
    private val blockNonContactsEnabled: Boolean,
    private val notificationHelper: NotificationHelper,
    private val searchRepository: SearchNetworkRepository
)  {
    private  val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
    private val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
    private val phoneNumber = formatPhoneNumber(phoneNumber)
    private val internetChecker = InternetChecker(context)


    suspend fun searchInServerAndHandle(hasedNum: String): Cntct = withContext(Dispatchers.IO) {
        val deferedSearchInSeraver = async { searchRepository.search(hasedNum) }
        var searchResult = Cntct("", phoneNumber, 0, "", "", "")
        try {
             val response = deferedSearchInSeraver.await()

            if(!response?.body()?.cntcts.isNullOrEmpty()){
                val result = response?.body()?.cntcts?.get(0)
                if(result!= null){
                    searchResult = Cntct(result.name?:"", phoneNumber, result.spammCount?:0, result.carrier?:"", result.location?:"", result.country?:"" )

                }




            }else{
                Log.d(TAG, "searchInServerAndHandle:empty response  ")
            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "searchInServerAndHandle:exception  $e")
        }
        return@withContext searchResult

    }
    suspend fun manageCall()  {
        /**
         * importatnt to launch using global scope because the
         * onReceive will return immediately and BroadcastReceiver will die before launch completes
         * https://stackoverflow.com/questions/58710363/cancel-coroutines-in-a-broadcastreceiver
         */
    CoroutineScope(Dispatchers.IO).launch {
        var deferedSearchInSeraver:Deferred<Response<SerachRes>?>? = null
   if( internetChecker.isnetworkAvailable()){
       val hashedNum = Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
       deferedSearchInSeraver = async { searchRepository.search(hashedNum) }
   }

    val deferedFindInContacts = async { contactAdressesDAO.find(phoneNumber) }


            val deferedBlockByPattern = async {
                var match = false
                blockedListpatternDAO.getAllBLockListPatternByFlow().collect {
                    for (item in it){
                        if(item.type == NUMBER_STARTS_WITH){
                            match =   phoneNumber.startsWith(item.numberPattern)
                        }else if(item.type == NUMBER_CONTAINING ){
                            match =  phoneNumber.contains(item.numberPattern)
                        }else{
                            match = phoneNumber.endsWith(item.numberPattern)
                        }
                        if(match){
                            endIncommingCall(context)

                        }
                    }
                }
    }
    try {
        deferedFindInContacts.await().apply {
            Log.d(TAG, "manageCall: deferedFindInContacts await")
            if (this == null) {
                if (blockNonContactsEnabled) {
                    endIncommingCall(context)
                    notificationHelper.showNotificatification(true, phoneNumber)
                }
            }
        }
      
//        context.startActivityIncommingCallView(res, phoneNumber)


    } catch (e: Exception) {
        Log.d(TAG, "manageCall: $e")
    }
    
    try {
        deferedBlockByPattern.await()
    }catch (e:Exception){
        Log.d(TAG, "manageCall: exception $e")
    }

    try{
        val res:Response<SerachRes>? =   deferedSearchInSeraver?.await()
        if(!res?.body()?.cntcts.isNullOrEmpty()){
            val result = res?.body()?.cntcts?.get(0)
//            if(result!!.spammCount > 0){
//                endIncommingCall(context)
//            }
        }
        Log.d(TAG, "manageCall: $res")
    }catch (e:Exception){
        Log.d(TAG, "manageCall: search in server exception $e")
    }
        }.join()

}


    private suspend fun blockByPattern(phoneNumber: String) {
        var match: Boolean
        blockedListpatternDAO.getAllBLockListPatternByFlow().collect {
            for (item in it){
                if(item.type == NUMBER_STARTS_WITH){
                    match =   phoneNumber.startsWith(item.numberPattern)
                }else if(item.type == NUMBER_CONTAINING ){
                    match =  phoneNumber.contains(item.numberPattern)
                }else{
                    match = phoneNumber.endsWith(item.numberPattern)
                }
                if(match){
                    endIncommingCall(context)
                    break
                }
            }
        }
    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
    }


     fun endIncommingCall(context: Context) {
         Log.d(TAG, "endIncommingCall: ")
        val c = CallEnder(context)
        c.endIncomingCall()
    }

    /**
     * increment the total number of calls blocked by hash caller in server
     * for analytics
     */
    @SuppressLint("LongLogTag")
    private suspend fun incrementTotalSpamCountByHashCallerInServer(
        searchRepository: SearchNetworkRepository
    ) {
        Log.d(TAG +"increment", "incrementTotalSpamCountByHashCallerInServer: ")
        searchRepository.incrementTotalSpamCount()
    }

    fun silenceIncomingCall(context: Context){
        val c = CallEnder(context)
        c.silenceIncomingCall()
    }

    suspend fun isBlockedByPattern(): Boolean  = withContext(Dispatchers.IO){
        var match = false
      for (item in blockedListpatternDAO.getAllBLockListPatternList()){

              if(item.type == NUMBER_STARTS_WITH){
                  match =   phoneNumber.startsWith(item.numberPattern)
              }else if(item.type == NUMBER_CONTAINING ){
                  match =  phoneNumber.contains(item.numberPattern)
              }else{
                  match = phoneNumber.endsWith(item.numberPattern)
              }
              if(match){
//                  endIncommingCall(context)
                    break
              }
             }
        return@withContext match

    }

    /**
     * if the function returns true block the call
     */
    suspend fun isNonContactsCallsAllowed(): Boolean  = withContext(Dispatchers.IO){
        var isBlock  = false
        val res = contactAdressesDAO.find(phoneNumber)
            if (res == null) {
                //this number not in contacts
                if (blockNonContactsEnabled) {
                    isBlock = true
//                    endIncommingCall(context)
//                    notificationHelper.showNotificatification(true, phoneNumber)
                }
            }
        return@withContext isBlock

    }


}