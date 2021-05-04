package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    init {

    }
//    private lateinit var  blockedLIstDao:BlockedLIstDao
//    private lateinit var mutedCallersDao: IMutedCallersDAO
//    private lateinit var blockListPatternRepository: BlockListPatternRepository
//
//    private lateinit var blockedListpatternDAO: BlockedLIstDao
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var  searchRepository: SearchNetworkRepository

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission", "LogNotTimber") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper = getNotificationHelper(context)

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {

            return
        }
        val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {

            val phoneNumber =
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val extraNetworkCountry = TelephonyManager.EXTRA_NETWORK_COUNTRY
            val actionNetworkCountryChanged =
                TelephonyManager.ACTION_NETWORK_COUNTRY_CHANGED
            if (phoneNumber == null) {
                return
            }

            /**
             * importatnt to launch using global scope because the
             * onReceive will return immediately and BroadcastReceiver will die before launch completes
             * https://stackoverflow.com/questions/58710363/cancel-coroutines-in-a-broadcastreceiver
             */

            GlobalScope.launch {
                var isSpam = false
                val inComingCallManager =   getIncomminCallManager(phoneNumber, context)
                val hasedNum = getHashedNum(phoneNumber, context)
                
                val defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(hasedNum) }
                val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                //todo also search for infor from server in local db about callers or a better way is if
                //to add number which are spam received from server info during worker,
                //insert them to blockpattern list, then check if block common spammers enabled
                try {
                    val isBlockedByPattern  = defBlockedByPattern.await()
                    if(isBlockedByPattern){
                        endCall(inComingCallManager, phoneNumber, context)

                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e")
                }
               
                try {
                    val resFromServer = defServerHandling.await()
                    if(resFromServer.spammCount?:0 > SPAM_THREASHOLD){
                        endCall(inComingCallManager, phoneNumber, context)

                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e ")
                }
                try {
                    if(defNonContactsBlocked.await()){
                        endCall(inComingCallManager, phoneNumber, context)

                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e")
                }
                
               
//                inComingCallManager.manageCall()

            }



        }
    }

    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String,
        context: Context
    ) {
        inComingCallManager.endIncommingCall(context)
        notificationHelper.showNotificatification(true, phoneNumber)
    }

    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))

        return  InCommingCallManager(context,
            phoneNumber, context.isBlockNonContactsEnabled(),
            notificationHelper, searchRepository)
    }

    private suspend  fun getHashedNum(phoneNumber: String, context: Context): String {
        return Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))

    }

    private fun getNotificationHelper(context: Context): NotificationHelper {

       return  NotificationHelper(context.isReceiveNotificationForSpamCallEnabled(), context)

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


    companion object {
//        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "__IncomingCallReceiver"
    }
}
