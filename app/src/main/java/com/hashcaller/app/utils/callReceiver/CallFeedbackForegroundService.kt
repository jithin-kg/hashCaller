package com.hashcaller.app.utils.callReceiver

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hashcaller.app.Secrets
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.NotificationHelper
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.utils.notifications.HashCaller
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.contacts.isActivityIncommingCallViewVisible
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*

/**
 * Service is needed to process incomming call in broadcast receiver, otherwise the
 * long running operations are getting cancelled
 * for refrence in foreground service use the link below
 * https://androidwave.com/foreground-service-android-example/
 * This foreground service starts the Incomming call view Activity
 */
class CallFeedbackForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"
    private lateinit var  searchRepository: SearchNetworkRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var inComingCallManager: InCommingCallManager
    private lateinit var countryCodeIso:String
    override fun onCreate() {
        countryCodeIso = CountrycodeHelper(this.applicationContext).getCountryISO()

    }
    companion object {
        var phoneNumber: String = ""
        const val TAG = "__ForegroundService"


        fun startService(context: Context, message: String, num: String) {
            phoneNumber = num
            val startIntent = Intent(context, CallFeedbackForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, CallFeedbackForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        Log.d(TAG, "onStartCommand: ")
//        createNotificationChannel()

       showNotification(intent)

//        this.closeIncommingCallView()

        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            supervisorScope.launch {
            if(isActivityIncommingCallViewVisible()){
//                sendBroadcast(Intent(EXPAND_INCOMMING_VIEW))
            }else{
                delay(1000L)
//                this@CallFeedbackForegroundService.startActivityIncommingCallView(
//                    null,
//                    phoneNumber,
//                    true
//                )
            }


                stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun showNotification(intent: Intent?) {
        val input = intent?.getStringExtra("inputExtra")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, HashCaller.CHANNEL_3_CALL_SERVICE_ID)
            .setContentTitle("Caller id is active")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_menu_call)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
        private suspend  fun getHashedNum(phoneNumber: String, context: Context): String {
        return Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))

    }
    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String,
        context: Context
    ) {
//        inComingCallManager.endIncommingCall(context)
//        notificationHelper.showNotificatification(true, phoneNumber)
    }
    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()

//        searchRepository = SearchNetworkRepository(
//            TokenManager(DataStoreRepository(context.tokeDataStore)),
//            tokenHelper
//        )
        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()

        return  InCommingCallManager(
            context,
            phoneNumber,
            null, searchRepository,
            internetChecker, blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO,
            countryCodeIso
        )
    }
}