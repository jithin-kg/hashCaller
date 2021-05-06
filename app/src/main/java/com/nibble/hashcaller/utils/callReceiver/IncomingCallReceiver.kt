package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.nibble.hashcaller.view.ui.call.ScreeningService
import java.lang.Exception


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    val ACTION_DO_STUFF = "action_do_stuff"

    init {

    }
    @SuppressLint("MissingPermission", "LogNotTimber") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {

       try {
           if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
               return
           }
           val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
           when(newState){
               TelephonyManager.EXTRA_STATE_RINGING -> {
                   //todo in jobscheduler check if screening role given, then the searching is not necessary
                   scheduleJobIncommingcaller(context, intent)

                   var roleHeld = false
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                       val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                       roleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
                       Log.d(TAG, "onReceive: role held ${roleHeld}")
                   }
               }
               TelephonyManager.EXTRA_STATE_IDLE -> {
                   scheduleJobCallEnded(context, intent)
               }
           }
           
       }catch (e:Exception){
           Log.d(TAG, "onReceive: exception $e")
       }
    }



    private fun scheduleJobCallEnded(context: Context, intent: Intent) {
        val phoneNumber =
            intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d(TAG, "scheduleJobCallEnded: $phoneNumber")
    }

    private fun scheduleJobIncommingcaller(context: Context, intent: Intent) {
        val phoneNumber =
            intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        val extraNetworkCountry = TelephonyManager.EXTRA_NETWORK_COUNTRY
        val actionNetworkCountryChanged =
            TelephonyManager.ACTION_NETWORK_COUNTRY_CHANGED
        if (phoneNumber == null) {
            return
        }
        Util.scheduleJob(context, phoneNumber);
    }


    companion object {
//        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "__IncomingCallReceiver"
    }
}
