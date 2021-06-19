package com.nibble.hashcaller.utils.callReceiver

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.R
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.utils.notifications.HashCaller.Companion.NOTIFICATION_ID
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import java.lang.Exception

class CallHandleWorker(private val context: Context, private val workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    private lateinit var  searchRepository: SearchNetworkRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var inComingCallManager: InCommingCallManager
    private lateinit var phoneNumber: String
    private  var rcAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "doWork: ")
            setForeground(createForegroundInfo())
            val supervisorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            supervisorScope.launch {

                var isSpam = false
                inComingCallManager =   getIncomminCallManager(phoneNumber, context)
                val hasedNum = getHashedNum(phoneNumber, context)
                //46a418e15711ea36c113b2fb9a157ade7aea9dd453254952428e7a2117f119c0
                //00c2551169dde5d78ee4c3424feef14e09a75d3b91d9e7e2f5878b370508dd65 worker
                Log.d("__hashedNumInReceiver", "onReceive: $hasedNum")

                val defServerHandling =  async {
                    hasedNum?.let {
                        inComingCallManager.searchInServerAndHandle(
                            it
                        )
                    }
                }
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
                        endCall(inComingCallManager, phoneNumber, context)
                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e")
                }

                try {
                    Log.d(TAG, "onReceive: second try")
                    val resFromServer = defServerHandling.await()
                    if(resFromServer?.spammCount?:0 > SPAM_THREASHOLD){
                        isSpam = true
                        endCall(inComingCallManager, phoneNumber, context)

                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e ")
                }
                try {
                    Log.d(TAG, "onReceive: third try ")
                    val r = defNonContactsBlocked.await()
                    if(r){
                        endCall(inComingCallManager, phoneNumber, context)

                    }
                }catch (e:Exception){
                    Log.d(TAG, "onReceive: $e")
                }
//                context.startActivityIncommingCallView(null, phoneNumber)

//                inComingCallManager.manageCall()

            }
            return Result.success()
        }catch (e:Exception) {
            Log.d(TAG, "doWork: exception $e")
            return Result.failure()
        }
    }

    /**
     * Create ForegroundInfo required to run a Worker in a foreground service.
     */
    private fun createForegroundInfo(): ForegroundInfo {
        // Use a different id for each Notification.
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    /**
     * Create the notification and required channel (O+) for running work
     * in a foreground service.
     */
    private fun createNotification(): Notification {
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(context).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(context, HashCaller.CHANNEL_3_CALL_SERVICE_ID)
            .setContentTitle("Hash caller")
            .setTicker("Caller id is active")
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_block_red)
            .setContentTitle("Call from ")
            .setContentText("Caller id is active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel(HashCaller.CHANNEL_3_CALL_SERVICE_ID, NOTIFICATION_CHANNEL_NAME).also {
//                builder.setChannelId(it.id)
//            }
//        }
        return builder.build()
    }

    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        searchRepository = SearchNetworkRepository(
            tokenHelper!!,
            callerInfoFromServerDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        )
        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()

        return  InCommingCallManager(
            context,
            phoneNumber,
            notificationHelper, searchRepository,
            internetChecker, blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO
        )
    }

    private suspend  fun getHashedNum(phoneNumber: String, context: Context): String? {
        var hash:String? = Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
//        hash = hashUsingArgon(hash)
        return  hash

    }
    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String,
        context: Context
    ) {
//        inComingCallManager.endIncommingCall(context)
//        notificationHelper.showNotificatification(true, phoneNumber)
    }
    companion object {
        const val TAG =  "__CallHandleWorker"
    }
}