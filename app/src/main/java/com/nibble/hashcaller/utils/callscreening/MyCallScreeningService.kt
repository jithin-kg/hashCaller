package com.nibble.hashcaller.utils.callscreening

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
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
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.callHandlers.base.extensions.parseCountryCode
import com.nibble.hashcaller.utils.callHandlers.base.extensions.removeTelPrefix
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.call.floating.Window
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.contacts.startFloatingService
import com.nibble.hashcaller.view.ui.contacts.startFloatingServiceFromScreeningService
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*

//https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
//https://zoransasko.medium.com/detecting-and-rejecting-incoming-phone-calls-on-android-9e0cff04ef20

private const val NOTIFICATION_CHANNEL_GENERAL = "quicknote_general"
private const val CODE_FOREGROUND_SERVICE = 1

/**
 * Note: A CallScreeningService must respond to a call within 5 seconds.
 * After this time, the framework will unbind from the CallScreeningService and ignore its response.
 */
@RequiresApi(Build.VERSION_CODES.N)
class MyCallScreeningService: CallScreeningService() {

    val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private  lateinit var helper: CallScreeningServiceHelper
    private lateinit var mCallDetails:Call.Details
    private lateinit var responseBuilder:CallResponse.Builder
    private lateinit var responseToCall:com.nibble.hashcaller.utils.callscreening.CallResponse
//    private  var _window:Window? = null
//    private  val window:Window get() = _window!!
    /**
     * important to look into CallScreeningService source code to findout how to work with this class
     */

//    rivate val notificationManager = NotificationManagerImpl()
    @SuppressLint("LongLogTag")
    override fun onScreenCall(callDetails: Call.Details) {
        mCallDetails = callDetails
        Log.d(TAG, "onScreenCall: ")
        val phoneNumber = getPhoneNumber(callDetails)
//        responseBuilder = CallResponse.Builder()
//        showNotification()
        startFloatingServiceFromScreeningService(phoneNumber)

//        _window = Window(this, phoneNumber)
//        window.open()
//        WindowObj.setWindow(window)
//        startCallScreeningForegroundService()
        supervisorScope.launch {
//            CallScreeningFloatingService.handleCall()

            val hashedNum =    getHashedNum(
                phoneNumber,
                this@MyCallScreeningService
            )
            helper = CallScreeningServiceHelper(
                getIncomminCallManager(phoneNumber, this@MyCallScreeningService),
                hashedNum,
                supervisorScope,
                phoneNumber,
                this@MyCallScreeningService
            ) { resToCall: Boolean -> { respondeToTheCall(resToCall) } }
            helper.handleCall()

//            stopForeground(true)
//            stopSelf()
//            delay(15000L)
//            stopForeground(true)
//            stopSelf()
            Log.d(TAG, "onScreenCall: after 10 seconds")
        }

    }

    private fun startCallScreeningForegroundService() {

        val intent = Intent(this, CallScreeningFloatingService::class.java)
//        phoneNumber?.let {
//            intent.putExtra(CONTACT_ADDRES, phoneNumber)
//            intent.putExtra(IntentKeys.INTENT_COMMAND, IntentKeys.START_FLOATING_SERVICE)

        intent.putExtra("response", responseToCall)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent)
            } else {
                this.startService(intent)
            }
//        }
    }

    @SuppressLint("LongLogTag")
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TAG, "onUnbind: ")
    }

    fun respondeToTheCall(isEndCall:Boolean){

//        if(isEndCall){
//            responseBuilder.setDisallowCall(true)
//        }
//        respondToCall(mCallDetails, responseBuilder.build())
    }


    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()

        val searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()

        return  InCommingCallManager(
            context,
            phoneNumber, context.isBlockNonContactsEnabled(),
            null, searchRepository,
            internetChecker, blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO
        )
    }

    /**
     * important to request this permission to show the alert on top of other apps / dialer
     */
    private fun startCallViewActivity() {
//        https://stackoverflow.com/questions/63509860/how-to-start-activity-from-callscreeningservice-in-java

//        val i = Intent(this, ActivityIncommingCallView::class.java)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        i.putExtra("name", "sample")
//        i.putExtra("phoneNumber", "808123")
//        i.putExtra("spamcount",0)
//        i.putExtra("carrier","sample")
//        i.putExtra("location", "sample")
//        startActivity(i)
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
    private fun showNotification() {

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        val exitIntent = Intent(this, FloatingService::class.java).apply {
//            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
//        }

//        val noteIntent = Intent(this, FloatingService::class.java).apply {
//            putExtra(INTENT_COMMAND, INTENT_COMMAND_NOTE)
//        }

//        val exitPendingIntent = PendingIntent.getService(
//            this, CODE_EXIT_INTENT, exitIntent, 0
//        )

//        val notePendingIntent = PendingIntent.getService(
//            this, CODE_NOTE_INTENT, noteIntent, 0
//        )

        // From Android O, it's necessary to create a notification channel first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                       NOTIFICATION_CHANNEL_GENERAL,
                        " getString(R.string.notification_channel_general)",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (ignored: Exception) {
                // Ignore exception.
            }
        }

        with(
            NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_GENERAL
            )
        ) {
            setTicker(null)
            setContentTitle(getString(R.string.app_name))
            setContentText("Caller ID is active")
            setAutoCancel(false)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.ic_baseline_call_24)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                priority = NotificationManager.IMPORTANCE_LOW
            }else{
                priority = Notification.PRIORITY_LOW
            }
//            setContentIntent(notePendingIntent)
//            addAction(
//                NotificationCompat.Action(
//                    0,
//                    "getString(R.string.notification_exit)",
//                    exitPendingIntent
//                )
//            )
            startForeground(CODE_FOREGROUND_SERVICE, build())
        }

    }

    @SuppressLint("LongLogTag")
    private   fun getHashedNum(phoneNumber: String, context: Context): String {
        var hashed = ""
        try {
            Log.d(TAG, "getHashedNum: phonenum $phoneNumber")
            hashed =  Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
            Log.d(TAG, "getHashedNum: $hashed")
            return hashed
        }catch (e:Exception){
            Log.d(TAG, "getHashedNum: $e")
        }
        return hashed
    }
    companion object {
        const val TAG = "__MyCallScreeningService"
    }


}