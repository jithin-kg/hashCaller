package com.hashcaller.app.view.ui.extensions

import android.app.Activity
import android.app.role.RoleManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.ROLE_SCREENING_APP_REQUEST_CODE
import com.hashcaller.app.view.ui.auth.PermissionRequestActivity

fun  AppCompatActivity.getCurrentDisplayMetrics(): DisplayMetrics {
    val dm = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(dm)
    return dm
}


/**
 * remember to call finish after calling this activity
 */
fun AppCompatActivity.startPermissionRequestActivity(){
    val i = Intent(this, PermissionRequestActivity::class.java)
    startActivity(i)

}

fun AppCompatActivity.requestAlertWindowPermission() {
    // Show alert dialog to the user saying a separate permission is needed
    if(!Settings.canDrawOverlays(applicationContext)){
        val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(myIntent)
    }

}


fun AppCompatActivity.getMyPopupMenu(menu: Int, anchorView: View): PopupMenu {

    val popup = PopupMenu(this, anchorView )
    popup.inflate(menu)
//    popup.setOnMenuItemClickListener(this)
//    popup.show()
    return popup
}

 fun AppCompatActivity.startContactEditActivity(contactId: Long) {
    val i = Intent(Intent.ACTION_EDIT)
    val contactUri =
        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
    i.data = contactUri
    i.putExtra("finishActivityOnSaveCompleted", true)
    startActivity(i)
}
@RequiresApi(Build.VERSION_CODES.Q)
    fun AppCompatActivity. requestScreeningRole(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager =  getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        if(!isHeld){
            //ask the user to set your app as the default screening app
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivityForResult(intent, ROLE_SCREENING_APP_REQUEST_CODE)
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
         val isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
           if(isRoleAvailable){
               roleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
           }else {
               //if screening role not available then set as role held
               roleHeld = true
           }

    }else {
        roleHeld = true
    }
    return roleHeld
}
fun Activity.requestScreeningRole(){
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