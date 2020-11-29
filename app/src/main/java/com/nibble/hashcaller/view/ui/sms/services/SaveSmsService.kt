package com.nibble.hashcaller.view.ui.sms.services

import android.app.IntentService
import android.content.ContentValues
import android.content.Intent
import com.nibble.hashcaller.view.ui.sms.util.SMSContract

class SaveSmsService : IntentService("SaveService") {
    override fun onHandleIntent(intent: Intent?) {
        val senderNo = intent!!.getStringExtra("sender_no")
        val message = intent.getStringExtra("message")
        val time = intent.getLongExtra("date", 0)
        val values = ContentValues()
        values.put("address", senderNo)
        values.put("body", message)
        values.put("date_sent", time)
        contentResolver.insert(SMSContract.ALL_SMS_URI, values)
        val i = Intent("android.intent.action.MAIN").putExtra("new_sms", true)
        this.sendBroadcast(i)
    }
}