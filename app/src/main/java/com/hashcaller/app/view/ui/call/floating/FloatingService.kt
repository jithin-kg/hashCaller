package com.hashcaller.app.view.ui.call.floating

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.R
import com.hashcaller.app.Secrets
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.Constants.Companion.NO_SIM_DETECTED
import com.hashcaller.app.utils.Constants.Companion.SIM_ONE
import com.hashcaller.app.utils.Constants.Companion.SIM_TWO
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.callReceiver.InCommingCallManager
import com.hashcaller.app.utils.callscreening.WindowObj
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.constants.IntentKeys.Companion.INTENT_COMMAND
import com.hashcaller.app.utils.constants.IntentKeys.Companion.PHONE_NUMBER
import com.hashcaller.app.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE
import com.hashcaller.app.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE_FROM_SCREENING_SERVICE
import com.hashcaller.app.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE_OFF_HOOK
import com.hashcaller.app.utils.constants.IntentKeys.Companion.STOP_FLOATING_SERVICE_FROM_INCOMMING_ACTVTY
import com.hashcaller.app.utils.constants.IntentKeys.Companion.STOP_FLOATIN_SERVICE_FROM_RECEIVER
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.contacts.*
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*
import java.util.*


//const val INTENT_COMMAND = "com.localazy.quicknote.COMMAND"
const val INTENT_COMMAND_EXIT = "EXIT"
const val INTENT_COMMAND_NOTE = "NOTE"

const val NOTIFICATION_CHANNEL_GENERAL = "Caller_id"
private const val CODE_FOREGROUND_SERVICE = 1
private const val CODE_EXIT_INTENT = 2
private const val CODE_NOTE_INTENT = 3
//https://localazy.com/blog/floating-windows-on-android-5-moving-window
class FloatingService: Service() {
    private lateinit var floatinServiceHelper:FloatinServiceHelper
    private  var window:Window? = null
    private var onStartCalled = false
    private var mphoneNumberStr = ""
    private  var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private lateinit var countryCodeIso:String
    private val libPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private var serviceStarted = false
    private var prevCallState:String? = null
    private var callEndedState:String = ""
    private var  dataStoreRepository:DataStoreRepository? = null
    private var callStateReceiver: BroadcastReceiver? = null

    //    private var token:String? = ""
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Remove the foreground notification and stop the service.
     */
    private fun stopService() {
        unregisterReceiver(callStateReceiver)
        stopForeground(true)
        stopSelf()
        window = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Use this function to create single  instances,
     * onCreate called only once, but
     */
    override fun onCreate() {
        super.onCreate()

        dataStoreRepository = DataStoreRepository(this.tokeDataStore)

        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        tokenHelper = TokenHelper(user)
        countryCodeIso = CountrycodeHelper(this.applicationContext).getCountryISO()

        if(window == null){
            window = Window(this, libPhoneCodeHelper)
            WindowObj.setWindow(window!!)
//                   windowCompanion = window
        }

        /**
         * This broadcast is send from incommingcallreceiver,
         *  this is important, sometimes notification is failed to close from within
         *  this service, so we need to make sure the notification is stopped when call state
         *  changed to IDLE
         */

        callStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context?, intent: Intent) {
                val  command = intent.getStringExtra(INTENT_COMMAND)?:""

                when(command){
                    STOP_FLOATING_SERVICE_FROM_INCOMMING_ACTVTY -> {
                        onCallEnded()

                        window?.close()
                        WindowObj.clearReference()
                        window = null
    //                                }
                        stopService()
                    }
                    STOP_FLOATIN_SERVICE_FROM_RECEIVER -> {
                        onCallEnded()

                        window?.close()
                        WindowObj.clearReference()
                        window = null
                        if(mphoneNumberStr.isNotEmpty()){
                            startActivityIncommingCallViewUpdated(mphoneNumberStr, callEndedState, callHandledSim, cntctForView)

                        }

    //                                }
                        stopService()
                    }
                }

            }
        }
        registerReceiver(callStateReceiver, IntentFilter(IntentKeys.BROADCAST_STOP_FLOATING_SERVICE))


    }

    /**
     * Called every time a startService of this service called
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        showNotification()
//          showForegroundNotification()
        val newState = intent.getStringExtra(IntentKeys.CALL_STATE)?:""
        if(newState.isNotEmpty()){
            prevCallState = newState
        }
        val command = intent.getStringExtra(INTENT_COMMAND)
        if(!serviceStarted){
            observeSubscriptionStatus(this)
            Log.d(TAG, "onStartCommand: service starting for firstime")
            serviceStarted = true
            super.onStartCommand(intent, flags, startId)
            val command = intent.getStringExtra(INTENT_COMMAND)
            if(command== IntentKeys.STOP_FLOATING_SERVICE_AND_WINDOW){
            }
//            else if(command == START_FLOATING_SERVICE_OFF_HOOK && !isCallScreeningRoleHeld()){
            else if(command == START_FLOATING_SERVICE_OFF_HOOK ){
                registerCallStateListener { phoneNumber, callState ->
                }
                  val num =   intent.getStringExtra(PHONE_NUMBER)
                num?.let{
                    window?.open()
                    mphoneNumberStr = it
                        onStartCalled = true
                    doHandleCall(it)
                }
            }
            else if(command == START_FLOATING_SERVICE){
//                if(!onStartCalled && !isCallScreeningRoleHeld()){
                if(!onStartCalled ){
                    registerCallStateListener { phoneNumber, callState ->
                        when(callState){
                            TelephonyManager.CALL_STATE_RINGING, TelephonyManager.CALL_STATE_OFFHOOK ->{
                                window?.open()
                                mphoneNumberStr = phoneNumber
                                onStartCalled = true
                                doHandleCall(phoneNumber)
                            }
                            TelephonyManager.CALL_STATE_IDLE -> {
                            }
                            TelephonyManager.CALL_STATE_OFFHOOK -> {
                                Log.d(TAG, "onStartCommand: offhook")
                            }

                        }
                    }
                }
            }else if(command == START_FLOATING_SERVICE_FROM_SCREENING_SERVICE){
                registerCallStateListener { phoneNumber, callState ->

                }
                val num = intent.getStringExtra(PHONE_NUMBER)
                num?.let {
                    window?.open()
                    mphoneNumberStr = it
                    onStartCalled = true
                    doHandleCall(mphoneNumberStr)
                }
                

            }
        }else {
            //floating service already started
            //check if window is visible in view, if window is not visible and
            //window has not been closed before start the window, this situation
            //can occur when user attend the call even before broadcast receiver
            //receives the call state
            if(!isWinManuallyClsd  && !isWindopwOpened){
                val command = intent.getStringExtra(INTENT_COMMAND)
                val num =   intent.getStringExtra(PHONE_NUMBER)?:""
                if(command == START_FLOATING_SERVICE_OFF_HOOK && num.isNotEmpty()){
                    window?.open()
                    mphoneNumberStr = num
                    onStartCalled = true
                    doHandleCall(mphoneNumberStr)
                }
            }
        }
        return START_STICKY
    }


    @SuppressLint("MissingPermission", "LogNotTimber")
    private fun observeSubscriptionStatus(context: Context) {
        var simHandlingCall = 0
        val telManager =  (context.getSystemService(Context.TELEPHONY_SERVICE) )as TelephonyManager

        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            // add phone state listener to the respective telemanager
           val availableSIMs =  subscriptionManager.activeSubscriptionInfoList

        //todo find real solution for android 11>=
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if(availableSIMs.size >1){
                val tel0 = telManager.createForSubscriptionId(availableSIMs[0].subscriptionId)
                val tel1 = telManager.createForSubscriptionId(availableSIMs[1].subscriptionId)
                tel0.listen(object :PhoneStateListener(){
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        super.onCallStateChanged(state, phoneNumber)
                        if(state ==  TelephonyManager.CALL_STATE_RINGING){
                            simHandlingCall = SIM_ONE
                            window?.setSimInView(SIM_ONE)
                        }else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
                            simHandlingCall = SIM_ONE
                            window?.setSimInView(SIM_ONE)

                        }else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                            window?.close()
                            WindowObj.clearReference()
                            window = null
                            startActivityIncommingCallViewUpdated(
                                mphoneNumberStr,
                                callEndedState,
                                callHandledSim,
                                cntctForView
                            )
                            stopService()
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)

                tel1.listen(object :PhoneStateListener(){
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        super.onCallStateChanged(state, phoneNumber)
                        if(state ==  TelephonyManager.CALL_STATE_RINGING){
                            simHandlingCall = SIM_TWO
                            window?.setSimInView(SIM_TWO)

                        }else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
                            simHandlingCall = SIM_TWO
                            window?.setSimInView(SIM_TWO)

                        }
                        else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {

                            window?.close()
                            WindowObj.clearReference()
                            window = null
                            startActivityIncommingCallViewUpdated(
                                mphoneNumberStr,
                                callEndedState,
                                callHandledSim,
                                cntctForView
                            )
                            stopService()
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)

            }else if(availableSIMs.size ==1) {
                val tel0 = telManager.createForSubscriptionId(availableSIMs[0].subscriptionId)
                tel0.listen(object :PhoneStateListener(){
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        super.onCallStateChanged(state, phoneNumber)
                        if(state ==  TelephonyManager.CALL_STATE_RINGING){
                            simHandlingCall = SIM_ONE
                            window?.setSimInView(SIM_ONE)

                        }else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
                            simHandlingCall = SIM_ONE
                            window?.setSimInView(SIM_ONE)
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)
            }else {
            }
        }


//        Log.d(TAG, "observeSubscriptionStatus: simHandlingCall $simHandlingCall")
        

    }

    private fun doHandleCall(phoneNumber: String) {

        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        supervisorScope.launch {
            window?.setPhoneNum(phoneNumber)
            val formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(phoneNumber), countryCodeIso)
            val hashedNum =    getHashedNum(formatedNum,
                this@FloatingService
            )
            hashedNum?.let {
                var spamThreshold:Int = dataStoreRepository?.getInt(PreferencesKeys.SPAM_THRESHOLD)?:DEFAULT_SPAM_THRESHOLD
                window?.setSpamThreshold(spamThreshold)
                floatinServiceHelper = FloatinServiceHelper(
                    getIncomminCallManager(formatedNum, this@FloatingService ,spamThreshold),
                    it,
                    supervisorScope,
                    window,
                    formatedNum,
                    this@FloatingService,
                    isCallScreeningRoleHeld(),
                    DataStoreRepository(tokeDataStore),
                    spamThreshold

                )
                Log.d(TAG, "doHandleCall: spamThreshold $spamThreshold")
                floatinServiceHelper.handleCall()
            }

        }
    }

    private fun registerCallStateListener(listener:(phoneNumber:String, callState:Int)-> Unit) {
        val telephony = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephony.listen(object : PhoneStateListener() {

            //            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
//                super.onCellInfoChanged(cellInfo)
//            }


            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                super.onCallStateChanged(state, incomingNumber)
                if (incomingNumber.isNotEmpty()) {
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING -> {
//                            startFloatingService(incomingNumber)
                            Log.d(TAG, "onCallStateChanged:inside ringing ")
                           if(incomingNumber.isNotEmpty()){
                               listener(incomingNumber, TelephonyManager.CALL_STATE_RINGING)
                           }

                        }
                        TelephonyManager.CALL_STATE_IDLE -> {
                            onCallEnded()
                            if(incomingNumber.isNotEmpty()){
                                window?.close()
                                    WindowObj.clearReference()
                                    window = null
                                    startActivityIncommingCallViewUpdated(
                                        incomingNumber,
                                        callEndedState,
                                        callHandledSim,
                                        cntctForView
                                    )
                                stopService()
                            }
                        }
                    }
                }

            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    /**
     * Called when call ends / floating service get stopped
     */
    private fun onCallEnded() {
        when(prevCallState){
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Log.d(TAG, "onCallEnded: miss call")
                callEndedState = "Missed call"
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "onCallEnded: call ended")
                callEndedState = "Call ended"

            }

        }
    }

    private fun getIncomminCallManager(phoneNumber: String, context: Context, spamThreshold: Int): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()

        val searchRepository = SearchNetworkRepository(
            tokenHelper,
            callerInfoFromServerDAO,
            libPhoneCodeHelper,
            countryCodeIso
        )

        val internetChecker = InternetChecker(context)
        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()

        return  InCommingCallManager(
            context,
            phoneNumber,
            null,
            searchRepository,
            internetChecker,
            blockedListpatternDAO,
            contactAdressesDAO,
            callerInfoFromServerDAO,
            countryCodeIso,
            spamThreshold

        )
    }




    private suspend fun getHashedNum(phoneNumber: String, context: Context): String? {
       var hashed:String? = ""
        try {
             hashed =  Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
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
        // From Android O, it's necessary to create a notification channel first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        "Caller Id",
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
            } catch (e: Exception) {
                // Ignore exception.
                Log.d(TAG, "showNotification: $e")
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
            }else {
                priority = Notification.PRIORITY_LOW
            }
            startForeground(CODE_FOREGROUND_SERVICE, build())
        }

    }



    companion object{
        private var callHandledSim = NO_SIM_DETECTED
        private var isWinManuallyClsd = false
        private var isWindopwOpened = false
        fun setWindowClosedManually(state: Boolean) {
            isWinManuallyClsd = state


        }

        fun setWindowOpened(state: Boolean) {
            isWindopwOpened = state
        }

        fun setSimCard(simNum: Int) {
            callHandledSim = simNum
        }


        const val TAG = "__FloatingService"

        var cntctForView: CntctitemForView = CntctitemForView(informationReceivedDate = Date())


    }
}