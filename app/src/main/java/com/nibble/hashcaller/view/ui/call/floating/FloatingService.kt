package com.nibble.hashcaller.view.ui.call.floating

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nibble.hashcaller.R
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.INTENT_COMMAND
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STOP_FLOATING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STOP_FLOATING_SERVICE_AND_WINDOW
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.*
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

//const val INTENT_COMMAND = "com.localazy.quicknote.COMMAND"
const val INTENT_COMMAND_EXIT = "EXIT"
const val INTENT_COMMAND_NOTE = "NOTE"

private const val NOTIFICATION_CHANNEL_GENERAL = "quicknote_general"
private const val CODE_FOREGROUND_SERVICE = 1
private const val CODE_EXIT_INTENT = 2
private const val CODE_NOTE_INTENT = 3
//https://localazy.com/blog/floating-windows-on-android-5-moving-window
class FloatingService    : Service() {
    private lateinit var floatinServiceHelper:FloatinServiceHelper
    private  var _window:Window? = null
    private  val window:Window get() = _window!!

        override fun onBind(intent: Intent?): IBinder? = null
    /**
     * Remove the foreground notification and stop the service.
     */
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }




    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        showNotification()

        val command:String? = intent.getStringExtra(INTENT_COMMAND)
//        // Exit the service if we receive the EXIT command.
//        // START_NOT_STICKY is important here, we don't want
//        // the service to be relaunched.
        command?.let {
            if(_window == null){
                _window = Window(this)
            }
//            _window = Window(this)
            if (command == STOP_FLOATING_SERVICE_AND_WINDOW) {
                window.close()
                stopService()
                return START_NOT_STICKY
            }else if(command == STOP_FLOATING_SERVICE){
//                stopService()
                //important to call stop foreground, this only removes the notification
                //calling StopService will result in unable to close window automaticallly when call ended
                stopForeground(true)
                return START_NOT_STICKY

            } else if(command == START_FLOATING_SERVICE){
                window.open()

                phoneNumber = intent.getStringExtra(CONTACT_ADDRES)
                // Be sure to show the notification first for all commands.
                // Don't worry, repeated calls have no effects.

                val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                supervisorScope.launch {
                    val hashedNum =    getHashedNum(phoneNumber,
                        this@FloatingService
                    )
                    floatinServiceHelper = FloatinServiceHelper(
                        getIncomminCallManager(FloatingService.phoneNumber, this@FloatingService)
                        ,this@FloatingService.isCallScreeningRoleHeld(),
                        hashedNum,
                        supervisorScope,
                        window,
                        phoneNumber,
                        this@FloatingService
                    )
                    floatinServiceHelper.handleCall()
                }
            }
        }



//        val window = Window(this)
//        window.open()
        // Show the floating window for adding a new note.


//        if (command == INTENT_COMMAND_NOTE) {
////            Toast.makeText(
////                this,
////                "Floating window to be added in the next lessons.",
////                Toast.LENGTH_SHORT
////            ).show()
//            val window = Window(this)
//            window.open()
//        }

        return START_STICKY
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




    companion object{
        const val TAG = "__FloatingService"
        var phoneNumber: String = ""
        fun startService(context: Context, message: String, num: String) {
            phoneNumber = num
//            val startIntent = Intent(context, FloatingService::class.java)
//            startIntent.putExtra("inputExtra", message)
//            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, FloatingService::class.java)
            context.stopService(stopIntent)
        }
    }
    private   fun getHashedNum(phoneNumber: String, context: Context): String {
       var hashed = ""
        try {
            Log.d(TAG, "getHashedNum: phonenum $phoneNumber")
             hashed =  Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
            Log.d(TAG, "getHashedNum: $hashed")
            //2fde9f69809082858fa7a55d441fde7ab7beea302204a437a793d2545fc381a9 ->123123
            return hashed
        }catch (e:Exception){
            Log.d(TAG, "getHashedNum: $e")
        }
       return hashed
    }


    /**
     * Create and show the foreground notification.
     */
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
}