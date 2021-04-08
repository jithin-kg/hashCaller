package com.nibble.hashcaller.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.utils.callHandlers.base.CallScreeningHelper
import com.nibble.hashcaller.utils.callHandlers.base.extensions.parseCountryCode
import com.nibble.hashcaller.utils.callHandlers.base.extensions.removeTelPrefix
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.sms.individual.util.BLOCK_INCOMMING_CALL
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

//https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
//https://zoransasko.medium.com/detecting-and-rejecting-incoming-phone-calls-on-android-9e0cff04ef20
@RequiresApi(Build.VERSION_CODES.N)
class MyCallScreeningService: CallScreeningService() {

    private lateinit var helper: CallScreeningHelper
    var blockedListpatternDAO: BlockedLIstDao =  this?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
    var contactAdressesDAO =  this?.let { HashCallerDatabase.getDatabaseInstance(it).contactAddressesDAO() }

    /**
     * important to look into CallScreeningService source code to findout how to work with this class
     */

//    rivate val notificationManager = NotificationManagerImpl()

    @SuppressLint("LongLogTag")
    override fun onScreenCall(callDetails: Call.Details) {

         helper = CallScreeningHelper(this, contactAdressesDAO)
        Log.d(TAG, "onScreenCall: ")

        val phoneNumber = getPhoneNumber(callDetails)
        var response = CallResponse.Builder()
        GlobalScope.launch {
            handleThisCall(phoneNumber, response, callDetails)
        }
        
//        response = handlePhoneCall(response, phoneNumber, callDetails)

    }

    private fun handleThisCall(
        phoneNumber: String,
        response: CallResponse.Builder,
        callDetails: Details
    ) = GlobalScope.launch {
        val formatedNum = formatPhoneNumber(phoneNumber)
            supervisorScope {
                val isMutedJob =  async {  helper.isMutedNumber(formatedNum) }
                val jobPatternBlock =  async { helper.isBlockedByPattern(formatedNum) }
                val jobNonContactBlock = async {
                    helper.isThisCallTobeBlocked(formatedNum,isBlockNonContactsEnabled()) }

                try {
                   isMutedJob.await().apply {
                       if(this){
                           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                               response.setSilenceCall(true)
                           }
                       }
                   }
                }catch (e:Exception){

                }
                val result1 =  try {
                    jobPatternBlock.await().apply { 
                        if(this){
                            response.setDisallowCall(true)
                            respondToCall(callDetails, response.build())
                            showNotificatification(true, formatedNum)
                        }
                    }
                }catch (e:Exception) {
                    false
                }
                val result2 =  try {
                    jobNonContactBlock.await().apply { 
                        if(this){
                            response.setDisallowCall(true)
                            respondToCall(callDetails, response.build())
                            showNotificatification(true, formatedNum)


                        }
                    }
                }catch (e:Exception){
                    false
                }

            }

    }


    /**
     * funcftion to handle notificatoin, if call blocked and user preference is to
     * to show notify for blocked calls , then show notification
     */
    @SuppressLint("LongLogTag")
    private fun showNotificatification(isBlocked: Boolean, phoneNumber: String) {
        var notificationManagerCmpt: NotificationManagerCompat = NotificationManagerCompat.from(this)
        if(isBlocked && isReceiveNotificationForSpamCallEnabled()){
            //show notification
            val resultIntent = Intent(this, MainActivity::class.java)
//            resultIntent.putExtra(CONTACT_ADDRES, senderNo)

// Create the TaskStackBuilder
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            }
            val notification = NotificationCompat
                .Builder(this, HashCaller.CHANNEL_2_ID )
                .setSmallIcon(R.drawable.ic_baseline_block_red)
                .setContentTitle("Call Blocked")
                .setContentText("Call from $phoneNumber is blocked")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManagerCmpt.notify(2, notification)

        }
    }

    private fun getPhoneNumber(callDetails: Call.Details): String {

        return callDetails.handle.toString().removeTelPrefix().parseCountryCode()

    }

    private fun displayToast(message: String) {
//        notificationManager.showToastNotification(applicationContext, message)
//        EventBus.getDefault().post(MessageEvent(message))
    }

    companion object {
        const val TAG = "__MyCallScreeningService"
    }
}