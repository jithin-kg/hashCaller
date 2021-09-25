package com.hashcaller.app.utils.callscreening

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.R
import com.hashcaller.app.Secrets
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.callHandlers.base.extensions.parseCountryCode
import com.hashcaller.app.utils.callHandlers.base.extensions.removeTelPrefix
import com.hashcaller.app.utils.callReceiver.InCommingCallManager
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.contacts.showNotifcationForSpamCall
import com.hashcaller.app.view.ui.contacts.startFloatingServiceFromScreeningService
import com.hashcaller.app.view.ui.contacts.stopFltinServiceFromActiivtyIncomming
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
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
    private   var helper: CallScreeningServiceHelper? = null
    private lateinit var mCallDetails:Call.Details
    private lateinit var responseBuilder:CallResponse.Builder
    private lateinit var responseToCall: com.hashcaller.app.utils.callscreening.CallResponse
    private  var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private lateinit var countryCodeIso :String
    private var phoneNumber = ""
    private lateinit var dataStoreRepository: DataStoreRepository
    //    private  var _window:Window? = null
//    private  val window:Window get() = _window!!


    /**
     * important to look into CallScreeningService source code to findout how to work with this class
     */

//    rivate val notificationManager = NotificationManagerImpl()
    @SuppressLint("LongLogTag")
    override fun onScreenCall(callDetails: Call.Details) {

        countryCodeIso = CountrycodeHelper(this.applicationContext).getCountryISO()
        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        tokenHelper = TokenHelper(user)
        mCallDetails = callDetails
        //todo check call direction to show outgoing call view quickly
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d(TAG, "onScreenCall: ${callDetails.callDirection}")

        }

         phoneNumber = getPhoneNumber(callDetails)
        phoneNumber = formatPhoneNumber(phoneNumber)
        phoneNumber = libCountryHelper.getES164Formatednumber(phoneNumber, countryCodeIso)
        responseBuilder = CallResponse.Builder()
//        showNotification()
//        startFloatingServiceFromScreeningService(phoneNumber)
        supervisorScope.launch {
            dataStoreRepository =  DataStoreRepository(tokeDataStore)
//            CallScreeningFloatingService.handleCall()

            val hashedNum =    getHashedNum(
                phoneNumber,
                this@MyCallScreeningService
            )

            helper = hashedNum?.let {
                val incomingCallManager = getIncomminCallManager(phoneNumber, this@MyCallScreeningService)
                CallScreeningServiceHelper(
                    incomingCallManager,
                    it,
                    supervisorScope,
                    phoneNumber,
                    this@MyCallScreeningService,
                    dataStoreRepository

                ) { resToCall: Boolean, reason:Int -> run { respondeToTheCallCallback(resToCall,reason) } }
            }
            helper?.handleCall()

//            stopForeground(true)
//            stopSelf()
//            delay(15000L)
//            stopForeground(true)
//            stopSelf()
            Log.d(TAG, "onScreenCall: after 10 seconds")
        }

    }


    @SuppressLint("LongLogTag")
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TAG, "onUnbind: ")
    }

    fun respondeToTheCallCallback(isEndCall:Boolean, reason:Int){
        supervisorScope.launch {
            withContext(Dispatchers.Main){
                if(isEndCall){
                    responseBuilder.setDisallowCall(true)
//                    stopFltinServiceFromActiivtyIncomming()
//                   stopFloatingService(true)
                }
                respondToCall(mCallDetails, responseBuilder.build())
                if(isEndCall){
                    //only show notiifcation for calls that needs to be blocked
                    showNotifcationForSpamCall(reason, phoneNumber)
                }

            }
        }

    }




    private suspend fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
        val callerInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        val spamThreshold = dataStoreRepository.getInt(SPAM_THRESHOLD)?: DEFAULT_SPAM_THRESHOLD
        val searchRepository = SearchNetworkRepository(
            tokenHelper,
            callerInfoFromServerDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
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





    private fun getPhoneNumber(callDetails: Call.Details): String {

        return callDetails.handle.toString().removeTelPrefix().parseCountryCode()

    }


    @SuppressLint("LongLogTag")
    private suspend fun getHashedNum(phoneNumber: String, context: Context): String? {
        var hashed:String? = ""
        try {
            Log.d(TAG, "getHashedNum: phonenum $phoneNumber")
            hashed =  Secrets().managecipher(context.packageName, formatPhoneNumber(phoneNumber))
//            hashed = hashUsingArgon(hashed)
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