package com.nibble.hashcaller.view.ui.contacts

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CARRIER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.COUNTRY
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.FIRST_NAME
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.LAST_NAME
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.LOCATION
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.PHONE_NUMBER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SHOW_FEEDBACK_VIEW
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SPAM_COUNT
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STATUS_CODE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.UPDATE_INCOMMING_VIEW
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_CALL_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_SMS_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_BLOCK_CONFIGURATIONS
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
import com.nibble.hashcaller.view.utils.SIMAccount
import com.nibble.hashcaller.work.formatPhoneNumber
import java.util.*


fun Context.closeIncommingCallView(){
//    val i = Intent(this, ActivityIncommingCallView::class.java)
//        i.putExtra("kill", 1)
//    startActivity(i)
}
fun Context.isActivityIncommingCallViewVisible():Boolean{
    return ActivityIncommingCallView.isVisible?:false
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
fun Context.startActivityIncommingCallView(cntc: CntctitemForView?, phoneNumber: String, showFeedbackView:Boolean = false) {
        val i = Intent(this, ActivityIncommingCallView::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra(FIRST_NAME, cntc?.firstName?:"")
        i.putExtra(LAST_NAME, cntc?.lastName?:"")
        i.putExtra(PHONE_NUMBER, phoneNumber?:"")

        i.putExtra(SPAM_COUNT, cntc?.spammCount?:0)
        i.putExtra(CARRIER, cntc?.carrier?:"")
        i.putExtra(LOCATION, cntc?.location?:"")
        i.putExtra(STATUS_CODE, cntc?.statusCode)
        i.putExtra(SHOW_FEEDBACK_VIEW, showFeedbackView)

//        i.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        startActivity(i)
        //if there is no info about the caller in server db
//        val i = Intent(this, ActivityIncommingCallView::class.java)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        i.putExtra("name", "")
//        i.putExtra("phoneNumber", phoneNumber)
//        i.putExtra("spamcount", "")
//        i.putExtra("carrier", "")
//        i.putExtra("location", "")
//        startActivity(i)
}
val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager


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

fun Context.isBlkForeignCallsEnabled():Boolean{
    val sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isBlockForeignCallsEnabled", false)
}

fun Context.isBlockTopSpammersAutomaticallyEnabled():Boolean{
    val sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean("isBlockTopSpamersAutomaticallyEnabled", false)
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




fun Context.getRandomColor(): Int {
    var random = 0
    val rand = Random()
    random = rand.nextInt(5 - 1) + 1
    return random
}

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