package com.hashcaller.utils.callReceiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.hashcaller.R
import com.hashcaller.Secrets
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.local.db.blocklist.BlockedLIstDao
import com.hashcaller.repository.search.SearchNetworkRepository
import com.hashcaller.utils.NotificationHelper
import com.hashcaller.utils.internet.InternetChecker
import com.hashcaller.utils.notifications.HashCaller
import com.hashcaller.view.ui.MainActivity
import com.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
class CallhandlService : JobIntentService(){
    private lateinit var  searchRepository: SearchNetworkRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var inComingCallManager: InCommingCallManager
    private lateinit var phoneNumber: String

    override fun onStopCurrentWork(): Boolean {
        return super.onStopCurrentWork()
    }

    override fun onHandleWork(intent: Intent) {
//        startForgroundSerive()
        try {
            if(isStopped){
                isServiceRunning = false
                return
                Log.d(TAG, "onHandleWork: isstopped")
            }else{
                isServiceRunning = true
                Log.d(TAG, "onHandleWork: not stoped")
            }
//            intent?.let {
                phoneNumber = "6505551212"
                notificationHelper = getNotificationHelper(this)

                /**
                 * importatnt to launch using global scope because the
                 * onReceive will return immediately and BroadcastReceiver will die before launch completes
                 * https://stackoverflow.com/questions/58710363/cancel-coroutines-in-a-broadcastreceiver
                 */
                val supervisorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                supervisorScope.launch {

                    var isSpam = false
                    inComingCallManager =   getIncomminCallManager(phoneNumber, this@CallhandlService)
                    val hasedNum = getHashedNum(phoneNumber, this@CallhandlService)
                    //46a418e15711ea36c113b2fb9a157ade7aea9dd453254952428e7a2117f119c0
                    //00c2551169dde5d78ee4c3424feef14e09a75d3b91d9e7e2f5878b370508dd65 worker
                    Log.d("__hashedNumInReceiver", "onReceive: $hasedNum")
                    val defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
                        hasedNum
                    ) }
                    val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                    val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                    //todo also search for infor from server in local db about callers or a better way is if
                    //to add number which are spam received from server info during worker,
                    //insert them to blockpattern list, then check if block common spammers enabled
                    try {
                        Log.d(TAG, "onReceive: firsttry")
                        val isBlockedByPattern  = defBlockedByPattern.await()
                        if(isBlockedByPattern){
                            isSpam = true
                            endCall(inComingCallManager, phoneNumber, this@CallhandlService)
                        }
                    }catch (e: Exception){
                        Log.d(TAG, "onReceive: $e")
                    }

                    try {
                        Log.d(TAG, "onReceive: second try")
                        val resFromServer = defServerHandling.await()
                        if(resFromServer?.spammCount?:0 > SPAM_THREASHOLD){
                            isSpam = true
                            endCall(inComingCallManager, phoneNumber, this@CallhandlService)

                        }
                    }catch (e: Exception){
                        Log.d(TAG, "onReceive: $e ")
                    }
                    try {
                        Log.d(TAG, "onReceive: third try ")
                        val r = defNonContactsBlocked.await()
                        if(r){
                            endCall(inComingCallManager, phoneNumber, this@CallhandlService)

                        }
                    }catch (e: Exception){
                        Log.d(TAG, "onReceive: $e")
                    }

//                context.startActivityIncommingCallView(null, phoneNumber)

//                inComingCallManager.manageCall()

                }
//            }
        }catch (e:Exception){
            Log.d(TAG, "onHandleWork: ")
        }finally {
            stopSelf()
        }



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
            notificationHelper, searchRepository,
            internetChecker, blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO,
            CountrycodeHelper(this.applicationContext).getCountryISO()
        )
    }

    private fun getNotificationHelper(context: Context): NotificationHelper {

        return  NotificationHelper(context.isReceiveNotificationForSpamCallEnabled(), context)

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

    private fun startForgroundSerive(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
////            createNotificationChannel(notificationManager)
////            notificationHelper.showNotificationForgroundCallService(phoneNumber)
//        }
        val  resultIntent= Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        }

//        val notificationBuilder = NotificationCompat.Builder(this, HashCaller.CHANNEL_3_CALL_SERVICE_ID)
        var notificationManagerCmpt: NotificationManagerCompat = NotificationManagerCompat.from(this)

        val notificationCmpt =  NotificationCompat.Builder(
            this,
            HashCaller.CHANNEL_3_CALL_SERVICE_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_block_red)
            .setContentTitle("Call Blocked")
            .setContentText("Call from  is blocked")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent)
            .build()

        startForeground(HashCaller.NOTIFICATION_ID, notificationCmpt)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
//        val channel = NotificationChannel(HashCaller.CHANNEL_3_CALL_SERVICE_ID, HashCaller.NOTIFICATION_CHANNEL_NAME,
//            NotificationManager.IMPORTANCE_LOW)

//        notificationManager.createNotificationChannel(channel)
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    companion object{
        const val TAG = "__CallhandlService"
        /**
         * Unique job ID for this service.
         */
        val JOB_ID = 1000

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, CallhandlService::class.java, JOB_ID, work)
        }
        fun isServiceCurrentlyRunning(): Boolean {
            return isServiceRunning
        }
        private var isServiceRunning = false

    }


}