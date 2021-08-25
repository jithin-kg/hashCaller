package com.hashcaller.app.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This onReceive is called when the message got in recipient phone /read by recipient
 */
class SmsStatusDeliveredReceiver : BroadcastReceiver() {
    var messageId:Long? = null
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")
        GlobalScope.launch {
            when (resultCode) {

                Activity.RESULT_OK -> {
                    Log.d(TAG, "onReceive: sms delivered")
//                    Toast.makeText(context, "Sms delivered",
//                        Toast.LENGTH_SHORT).show()
                    val uri = Uri.parse(intent.getStringExtra("message_uri"))

                    messageId = uri?.lastPathSegment?.toLong() ?: 0L
                 SmsStatusUpdator.updateMessageType(context, messageId!!, Telephony.Sms.MESSAGE_TYPE_SENT)
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "onReceive: sms not delivered")
//                    Toast.makeText(context, "Sms not delivered",
//                        Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    companion object{
        const val TAG="__SmsStatusDeliveredReceiver"
    }
}