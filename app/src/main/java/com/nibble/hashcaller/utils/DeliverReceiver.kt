package com.nibble.hashcaller.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.nibble.hashcaller.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This onReceive is called when the message got in recipient phone /read by recipient
 */
class DeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        GlobalScope.launch {
            when (resultCode) {

                Activity.RESULT_OK -> {
                    Log.d(TAG, "onReceive: sms delivered")
//                    Toast.makeText(context, "Sms delivered",
//                        Toast.LENGTH_SHORT).show()
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
        const val TAG="__DeliverReceiver"
    }
}