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
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.network.StatusCodes
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.callHandlers.base.CallScreeningHelper
import com.nibble.hashcaller.utils.callHandlers.base.extensions.parseCountryCode
import com.nibble.hashcaller.utils.callHandlers.base.extensions.removeTelPrefix
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isBlockTopSpammersAutomaticallyEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.contacts.startActivityIncommingCallView
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*

//https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
//https://zoransasko.medium.com/detecting-and-rejecting-incoming-phone-calls-on-android-9e0cff04ef20
@RequiresApi(Build.VERSION_CODES.N)
class MyCallScreeningService: CallScreeningService() {
    private lateinit var helper: CallScreeningHelper
    var blockedListpatternDAO: BlockedLIstDao =  this?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
    var contactAdressesDAO =  this?.let { HashCallerDatabase.getDatabaseInstance(it).contactAddressesDAO() }
    private val searchRepository:SearchNetworkRepository  =  SearchNetworkRepository(TokenManager(DataStoreRepository(this.tokeDataStore)
    ))
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
        CoroutineScope(Dispatchers.IO).launch {
            val hashedNum = Secrets().managecipher(this@MyCallScreeningService.packageName, formatPhoneNumber(phoneNumber))
            handleThisCall(phoneNumber, response, callDetails,hashedNum)
        }
        
//        response = handlePhoneCall(response, phoneNumber, callDetails)

    }

    /**
     * important to request this permission to show the alert on top of other apps / dialer
     */
    private fun startCallViewActivity() {
//        https://stackoverflow.com/questions/63509860/how-to-start-activity-from-callscreeningservice-in-java

        val i = Intent(this, ActivityIncommingCallView::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)


        i.putExtra("name", "sample")
        i.putExtra("phoneNumber", "808123")
        i.putExtra("spamcount",0)
        i.putExtra("carrier","sample")
        i.putExtra("location", "sample")
        startActivity(i)
    }

    @SuppressLint("LongLogTag")
    private fun handleThisCall(
        phoneNumber: String,
        response: CallResponse.Builder,
        callDetails: Details,
        hashedNum: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        val formatedNum = formatPhoneNumber(phoneNumber)
            supervisorScope {
                val deferedIsMutedJob =  async {  helper.isMutedNumber(formatedNum) }
                val deferedPatternBlock =  async { helper.isBlockedByPattern(formatedNum) }
                val deferedNonContactBlock = async {
                    helper.isThisCallTobeBlocked(formatedNum,isBlockNonContactsEnabled()) }

                val defredServerInfo = async { searchRepository.search(hashedNum) }
                try {
                   deferedIsMutedJob.await().apply {
                       if(this){
                           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                               response.setSilenceCall(true)
                           }
                       }
                   }
                }catch (e:Exception){

                }
                val result1 =  try {
                    deferedPatternBlock.await().apply {
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
                    deferedNonContactBlock.await().apply {
                        if(this){
                            response.setDisallowCall(true)
                            respondToCall(callDetails, response.build())
                            showNotificatification(true, formatedNum)


                        }
                    }
                    try{
                        val searchResponse = defredServerInfo.await()
                        val result = searchResponse?.body()?.cntcts

                        if(searchResponse?.body()?.status == StatusCodes.OK){

                            if(searchResponse?.body()?.cntcts !=null){
                                val cntctInfoFromserver = CntctitemForView(result?.firstName?:"", result?.lastName?:"", result?.carrier?:"",
                                    result?.location?:"", result?.lineType?:"",
                                    result?.country?:"",
                                    result?.spammCount?:0,
                                    searchResponse?.body()?.status?:0)

                                if(searchResponse!!.body()!!.cntcts!!.spammCount?:0 > SPAM_THREASHOLD){
                                    if(isBlockTopSpammersAutomaticallyEnabled()){
                                        response.setDisallowCall(true)
                                        respondToCall(callDetails, response.build())
                                        showNotificatification(true, formatedNum)

                                    }
                                    else{
                                        this@MyCallScreeningService.startActivityIncommingCallView(cntctInfoFromserver, phoneNumber)
                                        response.setDisallowCall(false)
                                        respondToCall(callDetails, response.build())
                                    }
                                }else{
                                    this@MyCallScreeningService.startActivityIncommingCallView(cntctInfoFromserver, phoneNumber)
                                    response.setDisallowCall(false)
                                    respondToCall(callDetails, response.build())
                                }
                            }

//                            startActivityIncommingCallView(cntctInfoFromserver, phoneNumber)
                        }
                        Log.d(TAG, "handleThisCall: ${searchResponse?.body()?.cntcts}")
                    }catch(e:Exception){
                        Log.d(TAG, "search in server : $e")
                    }
                }catch (e:Exception){
                    false
                }
            respondToCall(callDetails, response.build())
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