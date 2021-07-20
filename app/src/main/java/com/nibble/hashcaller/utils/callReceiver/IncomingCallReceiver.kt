package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.nibble.hashcaller.utils.constants.IntentKeys
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CALL_STATE
import com.nibble.hashcaller.view.ui.contacts.startFloatingService
import com.nibble.hashcaller.view.ui.contacts.startFloatingServiceOffhook
import kotlin.math.log


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

//          val telManager =  (context.getSystemService(Context.TELEPHONY_SERVICE) )as TelephonyManager
//
//           val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//
//
//           val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
//
//           val tel0 = telManager.createForSubscriptionId(availableSIMs[0].subscriptionId)
//           val tel1 = telManager.createForSubscriptionId(availableSIMs[1].subscriptionId)
//           Log.d(TAG, "onReceive:call state sim1 ${tel0.callState}")
//           Log.d(TAG, "onReceive:call state sim2 ${tel1.callState}")

//           telManager.callState
//           Log.d(TAG+"prev", "onReceive: prevState $prevState")
//           Log.d(TAG, "onReceive:carrier id ${intent.getStringExtra(TelephonyManager.EXTRA_CARRIER_ID)}")
//           Log.d(TAG, "onReceive:carrier id ${intent.getStringExtra(TelephonyManager.CARRieR)}")
           val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//           telephonyManager = context.defaultSubTelephonyManager.createForSubscriptionId(subId)
           prevState = state

           when(state){
                   TelephonyManager.EXTRA_STATE_RINGING -> {
                   Log.d(TAG, "onReceive: ringing :  startFloatingService")
                   context.startFloatingService(state)

               }
               TelephonyManager.EXTRA_STATE_OFFHOOK-> {
//                   prevState = OFFHOOK
//                   Log.d(TAG, "onReceive: hook num ${intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)}")
                   val num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)


                   Log.d(TAG, "onReceive: offhook $num")
                   if(!num.isNullOrEmpty()){
                       context.startFloatingServiceOffhook(num, state)
                   }
               }
               TelephonyManager.EXTRA_STATE_IDLE -> {
                   Log.d(TAG, "onReceive: idle sending broadcast")

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
