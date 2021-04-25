package com.nibble.hashcaller.view.ui.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.utils.callReceiver.SearchHelper
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_CALL_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_SMS_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_BLOCK_CONFIGURATIONS
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
import com.nibble.hashcaller.view.utils.SIMAccount
import com.nibble.hashcaller.work.formatPhoneNumber
import retrofit2.Response
import java.util.*


fun Context.startActivityIncommingCallView(res: Response<SerachRes>?, phoneNumber: String) {
    if(!res?.body()?.cntcts.isNullOrEmpty()){
        val result = res?.body()?.cntcts?.get(0)
        Log.d(SearchHelper.TAG, "searchForNumberInServer: result $result")

        val i = Intent(this, ActivityIncommingCallView::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra("name", result?.name)
        i.putExtra("phoneNumber", phoneNumber)
        i.putExtra("spamcount", result?.spammCount)
        i.putExtra("carrier", result?.carrier)
        i.putExtra("location", result?.location)
        startActivity(i)
    }else{
        //if there is no info about the caller in server db
        val i = Intent(this, ActivityIncommingCallView::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra("name", "")
        i.putExtra("phoneNumber", phoneNumber)
        i.putExtra("spamcount", "")
        i.putExtra("carrier", "")
        i.putExtra("location", "")
        startActivity(i)
    }
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