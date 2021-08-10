package com.hashcaller.utils

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction

class SMSsendWorkManager(private val context: Context,private val params:WorkerParameters ) :
    CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        try {
            val msg = inputData.getString("msg")
            val num = inputData.getString("phone_num")
            val settings = Settings()
            settings.useSystemSending = true;
            settings.deliveryReports = true //it is importatnt to set this for the sms delivered status

            val transaction = Transaction(context, settings)
            val message = Message(msg, "919495617494")
//        message.setImage(mBitmap);

            val smsSentIntent = Intent(context, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(context, SmsStatusDeliveredReceiver::class.java)
            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)
            transaction.sendNewMessage(message, 133)
        }catch (e:Exception){
            return  Result.retry()
        }
        return Result.success()
    }
}