package com.nibble.hashcaller.view.ui.contacts

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ContactListBinding
import com.nibble.hashcaller.datastore.PreferencesKeys
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.stubs.SimAndNumberDTO
import com.nibble.hashcaller.utils.Constants
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_BY_PATTERN
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_NON_CONTACT
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_TOP_SPAMMER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CALL_HANDLED_SIM
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CALL_HANDLED_STATE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CALL_STATE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CARRIER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.COUNTRY
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.FIRST_NAME
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.INTENT_COMMAND
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.LAST_NAME
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.LOCATION
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.PHONE_NUMBER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SHOW_FEEDBACK_VIEW
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SPAM_COUNT
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE_FROM_SCREENING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.START_FLOATING_SERVICE_OFF_HOOK
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STATUS_CODE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STOP_FLOATING_SERVICE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STOP_FLOATING_SERVICE_AND_WINDOW
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.UPDATE_INCOMMING_VIEW
import com.nibble.hashcaller.utils.notifications.blockPreferencesDataStore
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.floating.FloatingService
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.QUERY_STRING
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.search.SearchSMSActivity
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.utils.SIMAccount
import com.nibble.hashcaller.work.SpamReportWorker
import com.nibble.hashcaller.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*


fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun Context.showBadRequestToast(code: Int) {
    if(code in   (500 ..599)){
        toast(getString(R.string.server_error))
    }else if(code in (400 .. 499)){
        toast(getString(R.string.bad_request))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.hasMandatoryPermissions(): Boolean {
    return EasyPermissions.hasPermissions(
        this,
        READ_CONTACTS,
        READ_PHONE_STATE,
        CALL_PHONE,
//        READ_PHONE_NUMBERS
    )
}



fun Context.hasSMSReadPermission():Boolean{
    return EasyPermissions.hasPermissions(this, READ_CONTACTS,
        Manifest.permission.READ_SMS
    )
}

fun Context.hasReadContactsPermission(): Boolean {
    return EasyPermissions.hasPermissions(this,
        READ_CONTACTS
    )
}

fun Context.hasReadPhoneStatePermission():Boolean {
    return EasyPermissions.hasPermissions(this,
        READ_PHONE_STATE
        )
}

fun Context.hasReadCallLogPermission(): Boolean {
   return EasyPermissions.hasPermissions(this, Manifest.permission.READ_CALL_LOG,
        READ_CONTACTS
   )
}

fun Context.getAllCallLogsCursor(): Cursor? {
    var cursor:Cursor? = null

    val projection = arrayOf(
        CallLog.Calls.NUMBER,  //0
        CallLog.Calls.TYPE,    //1
        CallLog.Calls.DURATION,  //2
        CallLog.Calls.CACHED_NAME, //3
        CallLog.Calls._ID,         //4
        CallLog.Calls.DATE,        //5
        "subscription_id"
    )

    if(hasReadCallLogPermission()){
        cursor = contentResolver.query(
            CallLogLiveData.URI,
            projection,
            null,
            null,
            "${CallLog.Calls._ID} DESC"
        )
    }
    return cursor
}
fun Context.getAllContactsCursor(isLimitedListNeeded:Boolean = false): Cursor? {
    var cursor:Cursor? = null
    var  sortOrder: String = ""


    if(hasReadContactsPermission()){
        val projection = arrayOf(
            ContactsContract.Contacts.NAME_RAW_CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.PHOTO_URI
        )
        if(isLimitedListNeeded){
            sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC LIMIT 30"
        }else {
            sortOrder =  ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC"
        }
        cursor =  contentResolver.query(
            ContactLiveData.URI,
            projection,
            null,
            null,
            sortOrder
        )
    }
    return  cursor

}
fun Context.getAllSMSCursor(): Cursor? {
        var  cursor:Cursor? = null
    if(hasSMSReadPermission()){
        val projection = arrayOf(
            "thread_id",
            "_id",
            "address",
            "type",
            "body",
            "read",
            "date"
        )

       cursor =  contentResolver?.query(
            SMSContract.ALL_SMS_URI,
            projection,
            "address IS NOT NULL) GROUP BY (address",
            null,
            "_id DESC"
        )
    }

     return cursor
}

fun Context.startFloatingService(state: String?) {

    val intent = Intent(this, FloatingService::class.java)
        intent.putExtra(INTENT_COMMAND,START_FLOATING_SERVICE )
        intent.putExtra(CALL_STATE, state?:"")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }

}
fun Context.startFloatingServiceOffhook(num: String, state: String?) {

    val intent = Intent(this, FloatingService::class.java)
    intent.putExtra(INTENT_COMMAND,START_FLOATING_SERVICE_OFF_HOOK )
    intent.putExtra(PHONE_NUMBER, num)
    intent.putExtra(CALL_STATE, state?:"")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }

}

fun Context.startSpamReportWorker(contactAddress: String, spammerType: Int) {
    val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    val data = Data.Builder()
    data.putString(CONTACT_ADDRES, contactAddress)
    data.putInt(Constants.SPAMMER_TYPE, spammerType)

    val oneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SpamReportWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()
    WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
}

fun Context.startFloatingServiceFromScreeningService(phoneNumber: String) {

    val intent = Intent(this, FloatingService::class.java)
    intent.putExtra(INTENT_COMMAND,START_FLOATING_SERVICE_FROM_SCREENING_SERVICE )
    intent.putExtra(PHONE_NUMBER,phoneNumber )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }

}

/**
 * function to stop floating service and floating window based on
 * @param closeFloatingServiceAndWindow if  param is true close window and floatinwindow
 * else stop stop service only
 *
 */
fun Context.stopFloatingService(
    closeFloatingServiceAndWindow: Boolean = false
){
//    stopService(Intent(this, FloatingService::class.java))
//    stopService(Intent(this, FloatingService::class.java))
    if(closeFloatingServiceAndWindow){
        val exitIntent = Intent(this, FloatingService::class.java).apply {
            putExtra(INTENT_COMMAND, STOP_FLOATING_SERVICE_AND_WINDOW)
//            putExtra(CONTACT_ADDRES, incomingNumber)
        }

        startService(exitIntent)
    }else{
        val exitIntent = Intent(this, FloatingService::class.java).apply {
            putExtra(INTENT_COMMAND, STOP_FLOATING_SERVICE)
//            putExtra(CONTACT_ADDRES, incomingNumber)

        }
        startService(exitIntent)
    }

}

fun Context.startActivityIncommingCallView(
    phoneNumber: String,
    prevCallState: String?,
    callHandledSim: Int
) {

    val i = Intent(this, ActivityIncommingCallView::class.java)
    i.putExtra(PHONE_NUMBER, phoneNumber?:"")
    i.putExtra(CALL_HANDLED_STATE, prevCallState?:"" )
    i.putExtra(CALL_HANDLED_SIM, callHandledSim )
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK //Calling startActivity() from outside of an Activity  context requires the FLAG
    startActivity(i)
}
fun Context.closeIncommingCallView(){
//    val i = Intent(this, ActivityIncommingCallView::class.java)
//        i.putExtra("kill", 1)
//    startActivity(i)
}
fun Context.isActivityIncommingCallViewVisible():Boolean{
    return ActivityIncommingCallView.isVisible?:false
}

suspend fun Context.getBooleanFromSharedPref(key: String): Boolean {
    val wrapedKey =  booleanPreferencesKey(key)
    val tokenFlow: Flow<Boolean> = blockPreferencesDataStore.data.map {
        it[wrapedKey]?:false
    }
    return tokenFlow.first()
}

fun Context.showSnackBar(anchorView:View,
                         snackMessage:String,
                         actionMessage:String,
                         listener:View.OnClickListener,
                         duration:Int = Snackbar.LENGTH_INDEFINITE
                        ){
    val sBar = Snackbar.make(anchorView, snackMessage, duration)
    sBar.setAction(actionMessage, listener)
//    sBar.anchorView = anchorView
    sBar.show()
}
suspend fun Context.showNotifcationForSpamCall(reason: Int, phoneNumber: String) {
     if(getBooleanFromSharedPref(PreferencesKeys.RCV_NOT_BLK_CALL)){
         var content = ""
         Log.d("__Context", "showNotifcationForSpamCall:reason $reason")
         when(reason){
             InCommingCallManager.REASON_FOREIGN -> {
                 content = "Call from foreign country blocked."
             }

             REASON_BLOCK_NON_CONTACT -> {
                 content = "Call from non contact blocked."
             }
             REASON_BLOCK_TOP_SPAMMER -> {
                 content  = "Call identified as spam blocked."
             }
             REASON_BLOCK_BY_PATTERN -> {
                 content = "Call from black list blocked."
             }

         }
         NotificationHelper(true,
             this)
             ?.showNotificatification(
                 true,
                 phoneNumber,
                 content
             )
     }

}

/**
 * This is called to update incoming call view with search result from server
 * called from IncommingCallreceiver to get intent with extras result from server
 * @param resFromServer
 * @return intent with incomming caller info from server
 */
fun Context.getPreparedincommingIntent(
    resFromServer: CntctitemForView,
    phoneNumber: String,
    showFeedbackView: Boolean
): Intent {
    val intent = Intent(UPDATE_INCOMMING_VIEW) // this is used in incomming caller broadcast receiver to filter intetn
    intent.putExtra(FIRST_NAME, resFromServer.firstName?:"" )
    intent.putExtra(LAST_NAME, resFromServer.lastName?:"" )
    intent.putExtra(PHONE_NUMBER, phoneNumber )
    intent.putExtra(SPAM_COUNT, resFromServer.spammCount?:0 )
    intent.putExtra(CARRIER, resFromServer.carrier?:"")
    intent.putExtra(LOCATION, resFromServer.location?:0 )
    intent.putExtra(COUNTRY, resFromServer.country?:0 )
    intent.putExtra(STATUS_CODE, resFromServer?.statusCode)
    intent.putExtra(SHOW_FEEDBACK_VIEW, showFeedbackView)
    return intent
}

val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

// Manifest.permission.READ_PHONE_STATE is needed
@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("MissingPermission", "HardwareIds", "NewApi")
fun Context.getSimAndNumberPairList(): MutableList<SimAndNumberDTO> {
    val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
    val i =         subscriptionManager.opportunisticSubscriptions
    val info = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
    info.number
    val l = subscriptionManager.accessibleSubscriptionInfoList
    val listOfSimAndNumber : MutableList<SimAndNumberDTO> = mutableListOf()


    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val num = tm.line1Number

    Log.d("_Context", "getSimAndNumberPairList:$num ")

    if(availableSIMs.size > 0){
        for(subScriptionInfo in availableSIMs){

            listOfSimAndNumber.add(
                SimAndNumberDTO(subScriptionInfo.number, subScriptionInfo.carrierName.toString())
            )
        }
    }
    return listOfSimAndNumber
}
@SuppressLint("MissingPermission")
fun Context.getSimIndexForSubscriptionId(): List<String> {
    val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
    var simIds = mutableListOf<String>()
    if(availableSIMs.size > 0){
        for(subScriptionInfo in availableSIMs){
            simIds.add(subScriptionInfo.iccId)
        }
    }
    return simIds


}
@SuppressLint("MissingPermission")
fun Context.getAvailableSIMCardLabels(): ArrayList<SIMAccount> {
    val SIMAccounts = ArrayList<SIMAccount>()
    try {
        telecomManager.callCapablePhoneAccounts.forEachIndexed { index, account ->

            val phoneAccount = telecomManager.getPhoneAccount(account)

            var label = phoneAccount.label.toString()
            var address = phoneAccount.address.toString()
            if (address.startsWith("tel:") && address.substringAfter("tel:").isNotEmpty()) {
                address = Uri.decode(address.substringAfter("tel:"))
                label += " ($address)"
            }

            val SIM = SIMAccount(index + 1, phoneAccount.accountHandle, label, address.substringAfter("tel:"))
            SIMAccounts.add(SIM)
        }
    } catch (ignored: Exception) {
    }
    return SIMAccounts
}

fun Context.isVisible(view:View): Boolean {
    if(view.visibility== View.VISIBLE){
        return true
    }
    return false
}

fun Context.startSettingsActivity(activity: FragmentActivity?) {
    val intent = Intent(activity, SettingsActivity::class.java)
    startActivity(intent)
}

 fun Context.writeBoolToSharedPref(key: String, checked: Boolean, sharedPrefName:String) {
    val sharedPref = this?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE) ?: return
    with (sharedPref.edit()) {
        putBoolean(key, checked)
        apply()
    }
}


fun Context.isReceiveNotificationForSpamCallEnabled(): Boolean {
    val sharedpreferences = getSharedPreferences(SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean(IS_CALL_BLOCK_NOTIFICATION_ENABLED, false)
}

fun Context.isBlockNonContactsEnabled():Boolean{
    val sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isBlockNonContactCallsEnabled", false)

}


fun Context.isDefaultSMSHandler(): Boolean {
    var isDefaultSMSHandler = false
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager: RoleManager = this.getSystemService(RoleManager::class.java)
            // check if the app is having permission to be as default SMS app
            val isRoleAvailable =
                roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
            if (isRoleAvailable) {
                // check whether your app is already holding the default SMS app role.
                val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                isDefaultSMSHandler = isRoleHeld
            }
        } else {

            var isDefault = this.getPackageName() == Telephony.Sms.getDefaultSmsPackage(this)
            isDefaultSMSHandler = isDefault
        }
    }catch (e:java.lang.Exception) {
        Log.d("__Context", "isDefaultSMSHandler: $e")
        toast("Unable to identify default SMS role")
    }
    return isDefaultSMSHandler
}


/**
 * Called to check whether the application have Screening app role held
 * @return {@code true} if the app is default call screening app.
 * {@code false} if the app is not the default call screening app.
 */
 fun Context.isCallScreeningRoleHeld(): Boolean {
    var roleHeld = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        roleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }
    return roleHeld
}

fun Context.isReceiveNotificationForSpamSMSEnabled(): Boolean {
    val sharedpreferences = getSharedPreferences(SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean(IS_SMS_BLOCK_NOTIFICATION_ENABLED, false)
}

fun Context.makeCall(num:String){


    val formatedNum = "+" +formatPhoneNumber(num)
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    callIntent.data = Uri.parse("tel:$formatedNum")
    this.startActivity(callIntent)
}




//fun Context.getRandomColor(): Int {
//    var random = 0
//    val rand = Random()
//    random = rand.nextInt(5 - 1) + 1
//    return random
//}

fun Context.generateCircleView(num:Int?=null): Drawable? {
    var random = 0
    if(num==null){
        val rand = Random()
        random = rand.nextInt(5 - 1) + 1
    }else{
        random = num
    }
    var background : Drawable?= null
    when (random) {
        1 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background)
        }
        2 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background2)
        }
        3 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background3)
        }
        else -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background4)
        }
    }
    return background
}


fun Context.onContactItemClicked(binding: ContactListBinding, contactItem: Contact,activity:Activity? ){
    val intent = Intent(this, IndividualContactViewActivity::class.java )
    intent.putExtra(com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID, contactItem.phoneNumber)
    intent.putExtra("name", contactItem.firstName )
//        intent.putExtra("id", contactItem.id)
    intent.putExtra("photo", contactItem.photoURI)
    intent.putExtra("color", contactItem.drawable)
    val pairList = ArrayList<android.util.Pair<View, String>>()
//        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
    var pair:android.util.Pair<View, String>? = null
    if(contactItem.photoURI.isEmpty()){
        pair = android.util.Pair(binding.textViewcontactCrclr as View, "firstLetterTransition")
    }else{
        pair = android.util.Pair(binding.imgViewCntct as View,"contactImageTransition")
    }
    pairList.add(pair)
    val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0])
    startActivity(intent, options.toBundle())
}

fun Context.onSMSItemItemClicked(
    view: View,
    threadId: Long,
    pos: Int,
    pno: String,
    id: Long?,
    queryText: String
) {
    val intent = Intent(this, IndividualSMSActivity::class.java )
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    var bundle = Bundle()
    Log.d(SearchSMSActivity.TAG, "onContactItemClicked: chatId is $id")
    bundle.putString(CONTACT_ADDRES, pno)
    bundle.putString(SMS_CHAT_ID, id.toString())
    bundle.putString(QUERY_STRING,queryText)

    intent.putExtras(bundle)

    startActivity(intent)
}