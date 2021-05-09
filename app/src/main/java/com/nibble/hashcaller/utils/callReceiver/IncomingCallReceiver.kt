package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER
import android.util.Log
import com.nibble.hashcaller.view.ui.contacts.startActivityIncommingCallView
import com.nibble.hashcaller.view.ui.contacts.startFloatingService
import com.nibble.hashcaller.view.ui.contacts.stopFloatingService
import java.lang.Exception


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){
    
    @SuppressLint("MissingPermission", "LogNotTimber") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
       try {
              if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {
               return
                }
//           val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
           when(intent.getStringExtra(TelephonyManager.EXTRA_STATE)){
               TelephonyManager.EXTRA_STATE_RINGING -> {
                   Log.d(TAG, "onReceive: incomming")
//                   scheduleJobIncommingcaller(context, intent)
                   //icannot start a job because it is not always working
                  context.startFloatingService()

//                   context.startActivityIncommingCallView(null, intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER))
//                   Util.scheduleIncommingJob(context, intent.getStringExtra(EXTRA_INCOMING_NUMBER));
               }
               TelephonyManager.EXTRA_STATE_IDLE -> {
//                   Util.setPhoneNumInUtil("")
                   //call ended
//                   scheduleCallFeedbackJob(context, intent)
                  context.stopFloatingService()
               }
           }

       }catch (e:Exception){
           Log.d(TAG, "onReceive: exception $e")
       }
    }




    private fun scheduleCallFeedbackJob(context: Context, intent: Intent) {
//        val phoneNumber =
//            intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//        Log.d(TAG, "scheduleJobCallEnded: $phoneNumber")
//        Util.scheduleCallFeedbackJob(context, phoneNumber);
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
//        Util.scheduleIncommingJob(context, phoneNumber);
    }


    companion object {
//        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "__IncomingCallReceiver"
    }
}
