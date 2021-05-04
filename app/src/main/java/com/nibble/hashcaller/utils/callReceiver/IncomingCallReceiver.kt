package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled


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
            searchRepository = SearchNetworkRepository(context, DataStoreRepository(context))
            val inComingCallManager: InCommingCallManager = InCommingCallManager(context,
                phoneNumber, context.isBlockNonContactsEnabled(),
                notificationHelper, searchRepository)
            inComingCallManager.manageCall()



        }
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
