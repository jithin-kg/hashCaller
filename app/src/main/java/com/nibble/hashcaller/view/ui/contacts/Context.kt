package com.nibble.hashcaller.view.ui.contacts

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
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

fun Context.makeCall(num:String){
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    callIntent.data = Uri.parse("tel:$num")
    this.startActivity(callIntent)
}
@RequiresApi(Build.VERSION_CODES.Q)
fun AppCompatActivity. requestScreeningRole(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager =  getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        if(!isHeld){
            //ask the user to set your app as the default screening app
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivityForResult(intent, 123)
        } else {
            //you are already the default screening app!
        }
    }
}



/**
 * function to check if hashcaller is the call screening app
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun AppCompatActivity. isScreeningRoleHeld(): Boolean {
    var roleHeld = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager =  getSystemService(Context.ROLE_SERVICE) as RoleManager
        roleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }
    return roleHeld
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