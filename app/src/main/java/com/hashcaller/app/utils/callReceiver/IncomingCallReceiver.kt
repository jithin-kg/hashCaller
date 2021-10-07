package com.hashcaller.app.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.constants.IntentKeys.Companion.CALL_STATE
import com.hashcaller.app.view.ui.contacts.startFloatingService
import com.hashcaller.app.view.ui.contacts.startFloatingServiceOffhook


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    private var prevState:String? = null
    @SuppressLint( "LogNotTimber", "MissingPermission") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
       try {
              if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
               return
                }
           val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//           telephonyManager = context.defaultSubTelephonyManager.createForSubscriptionId(subId)
           prevState = state

           when(state){
                   TelephonyManager.EXTRA_STATE_RINGING -> {
                   context.startFloatingService(state)

               }
               TelephonyManager.EXTRA_STATE_OFFHOOK-> {
                   val num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)


                   if(!num.isNullOrEmpty()){
                       context.startFloatingServiceOffhook(num, state)
                   }
               }
               TelephonyManager.EXTRA_STATE_IDLE -> {
                   val stopIntent = Intent(IntentKeys.BROADCAST_STOP_FLOATING_SERVICE)
                   stopIntent.putExtra(
                       IntentKeys.INTENT_COMMAND,
                       IntentKeys.STOP_FLOATIN_SERVICE_FROM_RECEIVER
                   )
                   stopIntent.putExtra(CALL_STATE, state?:"")
                       context.sendBroadcast(stopIntent)
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
