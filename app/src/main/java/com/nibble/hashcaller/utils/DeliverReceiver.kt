package com.nibble.hashcaller.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.nibble.hashcaller.R

class DeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> Toast.makeText(context, "Sms delivered",
                Toast.LENGTH_SHORT).show()
            Activity.RESULT_CANCELED -> Toast.makeText(context, "Sms not delivered",
                Toast.LENGTH_SHORT).show()
        }
    }
}