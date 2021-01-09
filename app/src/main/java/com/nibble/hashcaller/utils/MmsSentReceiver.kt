package com.nibble.hashcaller.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log

class MmsSentReceiver : com.klinker.android.send_message.MmsSentReceiver() {
    override fun onMessageStatusUpdated(context: Context?, intent: Intent?, resultCode: Int) {
        super.onMessageStatusUpdated(context, intent, resultCode)
        if (resultCode == Activity.RESULT_OK) {
//            refreshMessages()
            Log.d(TAG, "onMessageStatusUpdated: ")
        }
    }
    companion object{
        const val TAG = "__MmsSentReceiver"
    }
}