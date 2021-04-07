package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_CALL_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_SMS_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
import java.util.*





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

fun Context.isReceiveNotificationForSpamSMSEnabled(): Boolean {
    val sharedpreferences = getSharedPreferences(SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS, Context.MODE_PRIVATE)
    return sharedpreferences.getBoolean(IS_SMS_BLOCK_NOTIFICATION_ENABLED, false)
}

fun Context.makeCall(num:String){
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    callIntent.data = Uri.parse("tel:$num")
    this.startActivity(callIntent)
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