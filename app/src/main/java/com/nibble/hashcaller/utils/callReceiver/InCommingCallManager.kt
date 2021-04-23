package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.contacts.startActivityIncommingCallView
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

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




    fun manageCall()  {
    CoroutineScope(Dispatchers.IO).launch {

    val deferedSearchInSeraver = async { searchRepository.search(phoneNumber) }
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
      val res =   deferedSearchInSeraver.await()
        if(!res?.body()?.cntcts.isNullOrEmpty()){
            val result = res?.body()?.cntcts?.get(0)
            if(result!!.spammCount > 0){
                endIncommingCall(context)
            }
        }
        context.startActivityIncommingCallView(res, phoneNumber)


    } catch (e: Exception) {
        Log.d(TAG, "manageCall: $e")
    }
    
    try {
        deferedBlockByPattern.await()
    }catch (e:Exception){
        Log.d(TAG, "manageCall: exception $e")
    }


        }

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


}