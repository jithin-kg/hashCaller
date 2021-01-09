package com.nibble.hashcaller.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.nibble.hashcaller.view.ui.contacts.utils.INTANT_SMS_BRECIEVER_TIME
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsStatusSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
           var act =  intent.action
        Log.d(TAG, "onReceive: ")
//        if(act.equals("SMS_SENT")){
//            Log.d(TAG, "onReceive:  SMS_SENT")
//
//        }

                when (resultCode) {
//                Activity.RESULT_OK -> {
//
//
////                Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show()
////                val id = intent.getIntExtra(INTANT_SMS_BRECIEVER_ID,-1)
////                val address = intent.getStringExtra("addressNo")
//                val extras = intent.extras
//                val time = extras?.getString("date")
//                    val address = extras?.getString("address")
//
////                val address = extras?.getString("addressNo")
//                    Log.d(TAG, "onReceive: time $time")
////                    Log.d(TAG, "onReceive: address $address")
////                Log.d(TAG, "onReceive, id: $id")
//
//
//                var extrasB = Bundle()
//                extrasB.putString("date", time!!)
//                    extrasB.putString("address", address)
////                extrasB.putString("addressNo", address)
//                // send data only if we have smsMessageStr
//                    Log.d(TAG, "onReceive:address $address")
//                val newIntent = Intent("myhashcallersms")
//                newIntent.putExtras(extrasB)
//
//                LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent)
//
////                val smsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsDAO() }
//                //update the message type from outbox to sent
//
////                val repository = context?.let { SMSLocalRepository(it) }
////                val values = ContentValues()
////                values.put("type", "2")
//
//
////                smsIndividualViewModel.moveToSent(id, address)
////                val res =  context.contentResolver.update(SMSContract.ALL_SMS_URI, values, "address='$address' AND _id=$id",null)
//
////                GlobalScope.launch {
////                    repository.moveFromoutBoxToSent(id, address!!)
////                }
////               GlobalScope.launch {  smsDAO.delete(id) }
//
//
//            }
//
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    Toast.makeText(context, "Generic failure",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onReceive: generic failure")
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
//                    Toast.makeText(context, "No service",
//                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onReceive: no service")
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {

                    Log.d(TAG, "onReceive: null pdu")
                    Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT)
                        .show()
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    Log.d(TAG, "onReceive: no netowork")
                    Toast.makeText(context, "No network",
                        Toast.LENGTH_SHORT).show()
                }
//
            }

    }

    companion object{
        private const val TAG = "__SmsStatusSentReceiver"
    }
}