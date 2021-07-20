package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.nibble.hashcaller.utils.constants.IntentKeys
import com.nibble.hashcaller.view.ui.contacts.startFloatingService
import com.nibble.hashcaller.view.ui.contacts.startFloatingServiceOffhook


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    private var prevState:Int? = null
    @SuppressLint("MissingPermission", "LogNotTimber") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
       try {
//              if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
//               return
//                }
           val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
           Log.d(TAG, "onReceive: $state")
           when(state){
                   TelephonyManager.EXTRA_STATE_RINGING -> {
                   context.startFloatingService()

               }
               TelephonyManager.EXTRA_STATE_OFFHOOK-> {
                   prevState = OFFHOOK
//                   Log.d(TAG, "onReceive: hook num ${intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)}")
                   val num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                   Log.d(TAG, "onReceive: offhook $num")
                   if(!num.isNullOrEmpty()){
                       context.startFloatingServiceOffhook(num)
                   }
               }
               TelephonyManager.EXTRA_STATE_IDLE -> {
                       val stopIntent = Intent(IntentKeys.BROADCAST_STOP_FLOATING_SERVICE)
                       context.sendBroadcast(stopIntent)
                       Log.d(TAG, "onReceive: idle")
               }
           }
       }catch (e: Exception){
           Log.d(TAG, "onReceive: exception $e")
       }
    }





    companion object {
        private const val TAG = "__IncomingCallReceiver"
        private const val OFFHOOK = 1
        private const val IDLE = 2

    }
}
