package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import java.lang.Exception


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    val ACTION_DO_STUFF = "action_do_stuff"
    val intent = Intent( )

    init {

    }
//    private lateinit var  blockedLIstDao:BlockedLIstDao
//    private lateinit var mutedCallersDao: IMutedCallersDAO
//    private lateinit var blockListPatternRepository: BlockListPatternRepository
//
//    private lateinit var blockedListpatternDAO: BlockedLIstDao
//    private lateinit var notificationHelper: NotificationHelper
//    private lateinit var  searchRepository: SearchNetworkRepository

    @SuppressLint("MissingPermission", "LogNotTimber") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
//        notificationHelper = getNotificationHelper(context)

//        goAsync()
       try {
           if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
               return
           }
           val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
           Log.d(TAG, "onReceive:state is  $newState")
           if (TelephonyManager.EXTRA_STATE_RINGING == newState) {
               val phoneNumber =
                   intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
               val extraNetworkCountry = TelephonyManager.EXTRA_NETWORK_COUNTRY
               val actionNetworkCountryChanged =
                   TelephonyManager.ACTION_NETWORK_COUNTRY_CHANGED
               if (phoneNumber == null) {
                   return
               }
               Util.scheduleJob(context, phoneNumber);
           }
       }catch (e:Exception){
           Log.d(TAG, "onReceive: exception $e")
       }
    }

    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String,
        context: Context
    ) {
//        inComingCallManager.endIncommingCall(context)
//        notificationHelper.showNotificatification(true, phoneNumber)
    }

//    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
//          val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()

//        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
//        val internetChecker = InternetChecker(context)
//         val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
//
//        return  InCommingCallManager(context,
//            phoneNumber, context.isBlockNonContactsEnabled(),
//            notificationHelper, searchRepository,
//            internetChecker, blockedListpatternDAO,
//            contactAdressesDAO
//            )
//    }

//    private suspend  fun getHashedNum(phoneNumber: String, context: Context): String {
//        return Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
//
//    }

//    private fun getNotificationHelper(context: Context): NotificationHelper {
//
//       return  NotificationHelper(context.isReceiveNotificationForSpamCallEnabled(), context)
//
//    }



    /**
     * increment the total number of calls blocked by hash caller in server
     * for analytics
     */
//    @SuppressLint("LongLogTag")
//    private suspend fun incrementTotalSpamCountByHashCallerInServer(
//        searchRepository: SearchNetworkRepository
//    ) {
//        Log.d(TAG +"increment", "incrementTotalSpamCountByHashCallerInServer: ")
//            searchRepository.incrementTotalSpamCount()
//    }


    companion object {
//        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "__IncomingCallReceiver"
    }
}
