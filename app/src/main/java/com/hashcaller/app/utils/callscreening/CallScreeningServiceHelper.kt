package com.hashcaller.app.utils.callscreening

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.hashcaller.app.R
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys.Companion.KEY_BLOCK_COMMONG_SPAMMERS
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.callReceiver.InCommingCallManager
import com.hashcaller.app.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_BY_PATTERN
import com.hashcaller.app.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_NON_CONTACT
import com.hashcaller.app.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_TOP_SPAMMER
import com.hashcaller.app.view.ui.contacts.utils.DATE_THREASHOLD
import com.hashcaller.app.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import kotlinx.coroutines.*
private const val NOTIFICATION_CHANNEL_GENERAL = "quicknote_general"
private const val CODE_FOREGROUND_SERVICE = 1

class CallScreeningServiceHelper(
    private val inComingCallManager: InCommingCallManager,
    private val hashedNum: String,
    private val supervisorScope: CoroutineScope,
    private val phoneNumber: String,
    private val context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val resToCallCallBack: (Boolean, Int) -> Unit) {
    var isSpam = false
    private var respondedToCall = false

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun  handleCall() = withContext(Dispatchers.IO){
        var isInfoFoundInCprovider = false
        supervisorScope.launch {
            val isBlockCommonSpammersEnabled =  dataStoreRepository.getSharedPreferencesBoolean(
                KEY_BLOCK_COMMONG_SPAMMERS
            )
            val spamThreshold = dataStoreRepository.getInt(SPAM_THRESHOLD)?: DEFAULT_SPAM_THRESHOLD
                 WindowObj.getWindowObj()?.setPhoneNum(phoneNumber)
                val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                val defBlockForeignCountryCalls = async { inComingCallManager.isBlockForeignCountryEnabled() }
                var defServerHandling:Deferred<CntctitemForView?>? = null
                val definfoAvaialbleInDb = async { inComingCallManager.getAvailbleInfoInDb() }

                val defredInfoFromCprovider = async { inComingCallManager.infoFromContentProvider() }
                try {
                    val contactInCprovider = defredInfoFromCprovider.await()
                    if(contactInCprovider!=null){
                        //the caller is in contact, so set information in db as caller information
                        isInfoFoundInCprovider = true
                        WindowObj.getWindowObj()?.updateWithcontentProviderInfo(contactInCprovider)

                    }
                    val infoAvailableInDb = definfoAvaialbleInDb.await()
                    if(infoAvailableInDb!=null){
                        if(infoAvailableInDb.spammCount?:0L > spamThreshold && isBlockCommonSpammersEnabled){
                            respondToSpamCall(REASON_BLOCK_TOP_SPAMMER)
                        }
                        if(!isInfoFoundInCprovider){
                            WindowObj.getWindowObj()?.updateWithServerInfo(infoAvailableInDb, phoneNumber)
                        }
                        if(isCurrentDateAndPrevDateisGreaterThanLimit(infoAvailableInDb.informationReceivedDate, DATE_THREASHOLD)){
                            defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(hashedNum) }
                        }

                    }else{
                         defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
                             hashedNum
                         ) }
                    }
                }catch (e:Exception){
                    Log.d(TAG, "handleCall: $e")
                }
                try {
                    Log.d(TAG, "onReceive: firsttry")
                    val isBlockedByPattern  = defBlockedByPattern.await()
                    if(isBlockedByPattern){
                        respondToSpamCall(REASON_BLOCK_BY_PATTERN)
                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }
                try {

                    Log.d(TAG, "onReceive: second try")
                    val resFromServer = defServerHandling?.await()
//                    if(resFromServer?.statusCode == HttpStatusCodes.STATUS_OK){
//                        WindowObj.getWindowObj()?.updateWithServerInfo(resFromServer, phoneNumber)
//                    }

                    if(resFromServer?.spammCount?:0 > spamThreshold && isBlockCommonSpammersEnabled){
                        respondToSpamCall(REASON_BLOCK_TOP_SPAMMER)
                    }
                    if(resFromServer!=null){
                        inComingCallManager.saveInfoFromServer(resFromServer, phoneNumber)
                    }

                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e ")
                }
                try {
                    Log.d(TAG, "onReceive: third try ")
                    val r = defNonContactsBlocked.await()
                    if(r){
                        respondToSpamCall(REASON_BLOCK_NON_CONTACT)

                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }
            try {
               if( defBlockForeignCountryCalls.await()){
                   respondToSpamCall(InCommingCallManager.REASON_FOREIGN)
               }
            }catch (e:Exception){
                Log.d(TAG, "handleCall:$e ")
            }




//            stopSelf();

        }.join()
        
        if(!respondedToCall){
            respondedToCall = true
            resToCallCallBack(false, 0)
        }
        delay(16000L)
        Log.d(TAG, "handleCall: after 6s")
//        context.stopFloatingService()
    }

    private suspend fun respondToSpamCall(reasonToBlock: Int) = withContext(Dispatchers.Main) {
        Log.d(TAG, "respondToSpamCall: blocking call")
        isSpam = true
        if(!respondedToCall){

            respondedToCall = true
            //check in preferences store, block common spammers enabled

            resToCallCallBack(true, reasonToBlock)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun showNotification() {

        val manager =context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                context,
                NOTIFICATION_CHANNEL_GENERAL
            )
        ) {
            setTicker(null)
            setContentTitle(context.getString(R.string.app_name))
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
            (context as CallScreeningService).startForeground(CODE_FOREGROUND_SERVICE, build())
        }

    }

    companion object{
        const val TAG = "__FloatinServiceHelper"

    }
}