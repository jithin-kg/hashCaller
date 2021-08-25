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
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.NotificationHelper
import com.hashcaller.app.utils.notifications.HashCaller
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.contacts.*
import com.hashcaller.app.view.ui.contacts.utils.SPAM_THREASHOLD
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*

/**
 * Service is needed to process incomming call in broadcast receiver, otherwise the
 * long running operations are getting cancelled
 * for refrence in foreground service use the link below
 * https://androidwave.com/foreground-service-android-example/
 * This foreground service starts the Incomming call view Activity
 */
class IncommingCallForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"
    private lateinit var  searchRepository: SearchNetworkRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var inComingCallManager: InCommingCallManager

    companion object {
        var phoneNumber: String = ""
        const val TAG = "__ForegroundService"
        fun startService(context: Context, message: String, num: String) {
            phoneNumber = num
            val startIntent = Intent(context, IncommingCallForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, IncommingCallForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        Log.d(TAG, "onStartCommand: ")
//        createNotificationChannel()
//        ActivityIncommingCallView.fa.

       showNotification(intent)

//        this.closeIncommingCallView()
        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            supervisorScope.launch {
                if (!this@IncommingCallForegroundService.isCallScreeningRoleHeld()) {
                    //start operations iff screening role not avaialble
                    Log.d(TAG, "onReceive: role not held")
//                    this@IncommingCallForegroundService.startActivityIncommingCallView(
//                        //start incomming call screen at start and show seraching... in view, then update with data from server
//                        CntctitemForView(statusCode = STATUS_SEARHING_IN_PROGRESS), phoneNumber)
//                delay(15000L)
                    var isSpam = false
//                    inComingCallManager =   getIncomminCallManager(phoneNumber, this@IncommingCallForegroundService)
                    val hasedNum = getHashedNum(phoneNumber, this@IncommingCallForegroundService)
                    val defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
                        hasedNum
                    )}

                    val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                    val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                    try {
                        Log.d(CallhandlService.TAG, "onReceive: firsttry")
                        val isBlockedByPattern  = defBlockedByPattern.await()
                        if(isBlockedByPattern){
                            isSpam = true
                            endCall(inComingCallManager, phoneNumber, this@IncommingCallForegroundService)
                        }
                    }catch (e: Exception){
                        Log.d(CallhandlService.TAG, "onReceive: $e")
                    }
                    try {

                        Log.d(CallhandlService.TAG, "onReceive: second try")
                        val resFromServer = defServerHandling.await()
                        if(resFromServer?.statusCode == HttpStatusCodes.STATUS_OK){
                            if(this@IncommingCallForegroundService.isActivityIncommingCallViewVisible()){
                                val intent =  this@IncommingCallForegroundService.getPreparedincommingIntent(resFromServer, phoneNumber, false)
                                sendBroadcast(intent)
                            }else{
//                                this@IncommingCallForegroundService.startActivityIncommingCallView(resFromServer, phoneNumber)

                            }

                        }
                        if(resFromServer?.spammCount?:0 > SPAM_THREASHOLD){
                            isSpam = true
                            endCall(inComingCallManager, phoneNumber, this@IncommingCallForegroundService)
                        }
//                    if(!resFromServer.firstName.isNullOrEmpty()){
//                        this@ForegroundService.startActivityIncommingCallView(resFromServer, phoneNumber)
//                    }


                    }catch (e: Exception){
                        Log.d(CallhandlService.TAG, "onReceive: $e ")
                    }
                    try {
                        Log.d(CallhandlService.TAG, "onReceive: third try ")
                        val r = defNonContactsBlocked.await()
                        if(r){
                            endCall(inComingCallManager, phoneNumber, this@IncommingCallForegroundService)

                        }
                    }catch (e: Exception){
                        Log.d(CallhandlService.TAG, "onReceive: $e")
                    }

                }


            stopSelf();

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
//    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
////        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
////
////        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
////        val internetChecker = InternetChecker(context)
////        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
////
////        return  InCommingCallManager(
////            context,
////            phoneNumber, context.isBlockNonContactsEnabled(),
////            null, searchRepository,
////            internetChecker, blockedListpatternDAO,
////            contactAdressesDAO,
////            callerInfoFromServerDAO
////        )
//    }
//    private lateinit var phoneNumber: String
//    private lateinit var  searchRepository: SearchNetworkRepository
//    private lateinit var notificationHelper: NotificationHelper
//    private lateinit var inComingCallManager: InCommingCallManager
//    override fun onCreate() {
//        Log.d(TAG, "onCreate: ")
//        super.onCreate()
//    }
//
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        showNotification(intent)
//        phoneNumber = intent.getStringExtra(CONTACT_ADDRES)
//        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//        supervisorScope.launch {
////                delay(15000L)
//            var isSpam = false
//            inComingCallManager =   getIncomminCallManager(phoneNumber, this@ForegroundService)
//            val hasedNum = getHashedNum(phoneNumber, this@ForegroundService)
//            val defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
//                hasedNum
//            ) }
//            val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
//            val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
//            try {
//                Log.d(CallhandlService.TAG, "onReceive: firsttry")
//                val isBlockedByPattern  = defBlockedByPattern.await()
//                if(isBlockedByPattern){
//                    isSpam = true
//                    endCall(inComingCallManager, phoneNumber, this@ForegroundService)
//                }
//            }catch (e: Exception){
//                Log.d(CallhandlService.TAG, "onReceive: $e")
//            }
//            try {
//                Log.d(CallhandlService.TAG, "onReceive: second try")
//                val resFromServer = defServerHandling.await()
//                if(resFromServer.spammCount?:0 > SPAM_THREASHOLD){
//                    isSpam = true
//                    endCall(inComingCallManager, phoneNumber, this@ForegroundService)
//
//                }
//            }catch (e: Exception){
//                Log.d(CallhandlService.TAG, "onReceive: $e ")
//            }
//            try {
//                Log.d(CallhandlService.TAG, "onReceive: third try ")
//                val r = defNonContactsBlocked.await()
//                if(r){
//                    endCall(inComingCallManager, phoneNumber, this@ForegroundService)
//
//                }
//            }catch (e: Exception){
//                Log.d(CallhandlService.TAG, "onReceive: $e")
//            }
//
//        }
//        Log.d(TAG, "onStartCommand: ")
//        //do heavy work on a background thread
//        stopSelf();
//        return START_NOT_STICKY
//    }
//    private fun endCall(
//        inComingCallManager: InCommingCallManager,
//        phoneNumber: String,
//        context: Context
//    ) {
//        inComingCallManager.endIncommingCall(context)
////        notificationHelper.showNotificatification(true, phoneNumber)
//    }
//    private fun showNotification(intent: Intent) {
//        val input = intent.getStringExtra(CONTACT_ADDRES)
//        createNotificationChannel()
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0, notificationIntent, 0
//        )
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("Foreground Service")
//            .setContentText(input)
//            .setSmallIcon(R.drawable.ic_menu_call)
//            .setContentIntent(pendingIntent)
//            .build()
//        startForeground(1, notification)
//    }
//
//    override fun onDestroy() {
//        Log.d(TAG, "onDestroy: ")
//        super.onDestroy()
//        stopForeground(true)
//
//    }
//
//    @Nullable
//    override fun onBind(intent: Intent?): IBinder? {
//        Log.d(TAG, "onBind: ")
//        return null
//    }
//
//    private fun createNotificationChannel() {
//        Log.d(TAG, "createNotificationChannel: ")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Foreground Service Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
//            manager?.createNotificationChannel(serviceChannel)
//        }
//    }
//    private suspend  fun getHashedNum(phoneNumber: String, context: Context): String {
//        return Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
//
//    }
//    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
//        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
//
//        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
//        val internetChecker = InternetChecker(context)
//        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
//
//        return  InCommingCallManager(
//            context,
//            phoneNumber, context.isBlockNonContactsEnabled(),
//            null, searchRepository,
//            internetChecker, blockedListpatternDAO,
//            contactAdressesDAO
//        )
//    }
//    companion object {
//        const val CHANNEL_ID = "ForegroundServiceChannel"
//        const val TAG = "__ForegroundService"
//    }
}