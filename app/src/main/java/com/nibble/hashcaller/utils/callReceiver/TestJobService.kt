package com.nibble.hashcaller.utils.callReceiver

import android.R
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.callReceiver.Util.scheduleJob
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.utils.notifications.HashCaller.Companion.NOTIFICATION_ID
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*


/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 * https://www.vogella.com/tutorials/AndroidTaskScheduling/article.html
 * refer Services.java library file for notification information
 */


class TestJobService : JobService() {
    private lateinit var  searchRepository: SearchNetworkRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var inComingCallManager: InCommingCallManager
    override fun onStartJob(params: JobParameters): Boolean {
//        val service = Intent(applicationContext, ForegroundService::class.java)
//        applicationContext.startService(service)
//        scheduleJob(applicationContext) // reschedule the job
//        Log.d(TAG, "onStartJob: ")

        ForegroundService.startService(this, "", "234342")
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0, notificationIntent, 0
//        )
//        val notification = NotificationCompat.Builder(this, HashCaller.CHANNEL_3_CALL_SERVICE_ID)
//            .setContentTitle("Foreground Service Kotlin Example")
//            .setContentText("input")
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setSmallIcon(R.drawable.ic_menu_call)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        startForeground(NOTIFICATION_ID, notification)
//        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//        supervisorScope.launch {
////                delay(15000L)
//            var isSpam = false
//            inComingCallManager =   getIncomminCallManager(phoneNumber, this@TestJobService)
//            val hasedNum = getHashedNum(ForegroundService.phoneNumber, this@TestJobService)
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
//                    endCall(inComingCallManager, phoneNumber, this@TestJobService)
//                }
//            }catch (e: Exception){
//                Log.d(CallhandlService.TAG, "onReceive: $e")
//            }
//            try {
//                Log.d(CallhandlService.TAG, "onReceive: second try")
//                val resFromServer = defServerHandling.await()
//                if(resFromServer.spammCount?:0 > SPAM_THREASHOLD){
//                    isSpam = true
//                    endCall(inComingCallManager,phoneNumber, this@TestJobService)
//
//                }
//            }catch (e: Exception){
//                Log.d(CallhandlService.TAG, "onReceive: $e ")
//            }
//            try {
//                Log.d(CallhandlService.TAG, "onReceive: third try ")
//                val r = defNonContactsBlocked.await()
//                if(r){
//                    endCall(inComingCallManager,phoneNumber, this@TestJobService)
//
//                }
//            }catch (e: Exception){
//                Log.d(TAG, "onReceive: $e")
//            }
//
//
//            Log.d(TAG, "onStartCommand: after a delay")
////            delay(500L)
//            NotificationManagerCompat.from(this@TestJobService).cancel(null,NOTIFICATION_ID );
//        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        stopSelf();

        return false // false ->indicates that job should not be restarted when stopped
    }
    private suspend  fun getHashedNum(phoneNumber: String, context: Context): String {
        return Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))

    }
    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String,
        context: Context
    ) {
        inComingCallManager.endIncommingCall(context)
//        notificationHelper.showNotificatification(true, phoneNumber)
    }
    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()

        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()

        return  InCommingCallManager(
            context,
            phoneNumber, context.isBlockNonContactsEnabled(),
            null, searchRepository,
            internetChecker, blockedListpatternDAO,
            contactAdressesDAO
        )
    }
    companion object {
        private const val TAG = "SyncService"
        var phoneNumber: String = ""

    }
}