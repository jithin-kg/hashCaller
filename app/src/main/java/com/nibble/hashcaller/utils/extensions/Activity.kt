package com.nibble.hashcaller.utils.extensions

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.search.SearchActivity
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.SET_DEF_SMS_REQ_CODE

fun Activity.startSearchActivity() {
    val intent = Intent(this, SearchActivity::class.java)
    startActivity(intent)
}
fun Activity.requestDefaultSMSrole() {
    var isDefault = false
    try{
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager: RoleManager? = getSystemService(android.app.role.RoleManager::class.java)
            // check if the app is having permission to be as default SMS app
            val isRoleAvailable =
                roleManager?.isRoleAvailable(android.app.role.RoleManager.ROLE_SMS)
            if (isRoleAvailable == true) {
                // check whether your app is already holding the default SMS app role.
                val isRoleHeld = roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_SMS)
                if (!isRoleHeld) {
//                        is not defauls sms app, so show request button
                    val roleRequestIntent =
                        roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_SMS)
                    startActivityForResult(roleRequestIntent, SET_DEF_SMS_REQ_CODE)
//                            layoutSend.beInvisible()
//                            btnMakeDefaultSMS.beVisible()
                }
//                    else{
////                            layoutSend.beInvisible()
////                            btnMakeDefaultSMS.beVisible()
//                        requesetPermission()
//                    }
            }
        } else {
            val intent = Intent(android.provider.Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(android.provider.Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivityForResult(intent, SET_DEF_SMS_REQ_CODE)
        }
    }catch (e: Exception){
        android.util.Log.d("__Activity", "checkDefaultSettings: exception $e")
    }
//            return isDefault
}

fun Activity.startIndividualSMSActivityByAddress(address: String, view: View?= null) {

    val intent = Intent(this, IndividualSMSActivity::class.java )
    val bundle = Bundle()
    bundle.putString(CONTACT_ADDRES, address)
    bundle.putString(SMS_CHAT_ID, "")

    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    intent.putExtras(bundle)
    startActivity(intent)
}