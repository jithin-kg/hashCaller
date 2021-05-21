package com.nibble.hashcaller.view.ui.call.floating

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.R
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.utils.callscreening.WindowObj
import com.nibble.hashcaller.utils.constants.IntentKeys
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.INTENT_COMMAND
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE_FROM_SCREENING_SERVICE
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.blockPreferencesDataStore
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.*
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.hashUsingArgon
import com.nibble.hashcaller.view.utils.LibCoutryCodeHelper
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
class FloatingService: Service() {
    private lateinit var floatinServiceHelper:FloatinServiceHelper
    private  var window:Window? = null

    private var onStartCalled = false
    private var mphoneNumberStr = ""
    private var countryCodeHelper: LibCoutryCodeHelper? = null
    private  var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
//    private var token:String? = ""
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Remove the foreground notification and stop the service.
     */
    private fun stopService() {
        stopForeground(true)
        stopSelf()
        window = null
    }

    /**
     * Use this function to create single  instances,
     * onCreate called only once, but
     */
    override fun onCreate() {
        super.onCreate()
        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        tokenHelper = TokenHelper(user)

        countryCodeHelper = LibCoutryCodeHelper(PhoneNumberUtil.getInstance())

        if(window == null){
            window = Window(this, countryCodeHelper)
            WindowObj.setWindow(window!!)
//                    windowCompanion = window
        }

    }

    /**
     * Called every time a startService of this service called
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        showNotification()
        super.onStartCommand(intent, flags, startId)

        val command = intent.getStringExtra(INTENT_COMMAND)
            if(command== IntentKeys.STOP_FLOATING_SERVICE_AND_WINDOW){
                window?.close()
                WindowObj.clearReference()
                window = null
                startActivityIncommingCallView(mphoneNumberStr)
                stopService()
            }
            else if(command == START_FLOATING_SERVICE){
                if(!onStartCalled){
                    window?.open()
                    registerCallStateListener { phoneNumber, callState ->
                        when(callState){
                            TelephonyManager.CALL_STATE_RINGING ->{
                                mphoneNumberStr = phoneNumber
                                onStartCalled = true

                                if(command == START_FLOATING_SERVICE){
                                    Log.d(TAG, "onStartCommand: window opening")
                                    // Be sure to show the notification first for all commands.
                                    // Don't worry, repeated calls have no effects.
                                    doHandleCall(phoneNumber)
//                        return START_NOT_STICKY
                                }
//            }
//        }

//        else{
////            if(command == STOP_FLOATING_SERVICE_AND_WINDOW){
////                startActivityIncommingCallView(null, phoneNumber)
////            }
////            stopService()
//        }
                            }
                            TelephonyManager.CALL_STATE_IDLE -> {

//                                stopService()
//                                Log.d(TAG, "idle callback : ")
//                                window.close()
                            }
                        }
                    }
//        val command:String? = intent.getStringExtra(INTENT_COMMAND)
//        phoneNumber = intent.getStringExtra(CONTACT_ADDRES)






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
                }
            }else if(command == START_FLOATING_SERVICE_FROM_SCREENING_SERVICE){
                window?.open()
                mphoneNumberStr = intent.getStringExtra(CONTACT_ADDRES)
                onStartCalled = true
                Log.d(TAG, "onStartCommand: numfromservice: $mphoneNumberStr")
                doHandleCall(mphoneNumberStr)
            }



        return START_STICKY
    }

    private fun doHandleCall(phoneNumber: String) {

        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        supervisorScope.launch {
            window?.setPhoneNum(phoneNumber)
            val hashedNum =    getHashedNum(phoneNumber,
                this@FloatingService
            )
            hashedNum?.let {
                floatinServiceHelper = FloatinServiceHelper(
                    getIncomminCallManager(phoneNumber, this@FloatingService),
                    it,
                    supervisorScope,
                    window,
                    phoneNumber,
                    this@FloatingService,
                    isCallScreeningRoleHeld(),
                    DataStoreRepository(blockPreferencesDataStore)
                )
                floatinServiceHelper.handleCall()
            }

        }
    }

    private fun registerCallStateListener(listener:(phoneNumber:String, callState:Int)-> Unit) {

        val telephony = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
//                Log.d(TAG, "onCallStateChanged: ")
                super.onCallStateChanged(state, incomingNumber)
                if (incomingNumber.isNotEmpty()) {
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING -> {
                            Log.d(TAG, "onCallStateChanged:ringing $incomingNumber ")
//                            startFloatingService(incomingNumber)
                           if(incomingNumber.isNotEmpty()){
                               listener(incomingNumber, TelephonyManager.CALL_STATE_RINGING)
                           }

                        }
                        TelephonyManager.CALL_STATE_IDLE -> {
                            Log.d(TAG, "onCallStateChanged: idle $incomingNumber")
                            if(incomingNumber.isNotEmpty()){
                                listener(incomingNumber, TelephonyManager.CALL_STATE_RINGING)
                            }
//                            stopFloatingService(true, incomingNumber)
                        }
//                        TelephonyManager.CALL_STATE_OFFHOOK -> {
//                            Log.d(TAG, "onCallStateChanged: ofhook $incomingNumber")
//                        }
//                        TelephonyManager.EXTRA_CARRIER_NAME
                    }
                }

            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()

        val searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)), tokenHelper)
        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()

        return  InCommingCallManager(
            context,
            phoneNumber, context.isBlockNonContactsEnabled(),
            null,
            searchRepository,
            internetChecker,
            blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO,
        )
    }




    private suspend fun getHashedNum(phoneNumber: String, context: Context): String? {
       var hashed:String? = ""
        try {
            Log.d(TAG, "getHashedNum: phonenum $phoneNumber")
             hashed =  Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
            hashed = hashUsingArgon(hashed)
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



    companion object{
        const val TAG = "__FloatingService"


//        var windowCompanion:Window? = null
//        var phoneNumber: String = ""
//        fun startService(context: Context, message: String, num: String) {
////            phoneNumber = num
////            val startIntent = Intent(context, FloatingService::class.java)
////            startIntent.putExtra("inputExtra", message)
////            ContextCompat.startForegroundService(context, startIntent)
////        }
//        fun stopService(context: Context) {
//            val stopIntent = Intent(context, FloatingService::class.java)
//            context.stopService(stopIntent)
//        }
//        fun getInflatedWindow(): Window? {
//            return windowCompanion
//        }
    }
}