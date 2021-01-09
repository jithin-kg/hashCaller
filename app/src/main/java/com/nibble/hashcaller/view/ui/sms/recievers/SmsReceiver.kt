package com.nibble.hashcaller.view.ui.sms.recievers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_NAME
import com.nibble.hashcaller.view.ui.contacts.utils.FROM_SMS_RECIEVER
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.services.SaveSmsService
import com.nibble.hashcaller.view.utils.DefaultFragmentManager


class SmsReceiver : BroadcastReceiver() {
    private val TAG = "__SmsReceiver"
    private lateinit var notificationManagerCmpt:  NotificationManagerCompat
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {

            Log.e(TAG, "smsReceiver")

            notificationManagerCmpt = NotificationManagerCompat.from(context)

            val bundle = intent.extras
            if (bundle != null) {
                val pdu_Objects = bundle["pdus"] as Array<Any>?
                if (pdu_Objects != null) {
                    for (aObject in pdu_Objects) {
                        val currentSMS = getIncomingMessage(aObject, bundle)
                        val senderNo = currentSMS.displayOriginatingAddress
                        val message = currentSMS.displayMessageBody

                        //Log.d(TAG, "senderNum: " + senderNo + " :\n message: " + message);
//                        issueNotification(context, senderNo, message)

                        showNotification(context, senderNo, message)
                        saveSmsInInbox(context, currentSMS)
                    }
                    abortBroadcast()
                    // End of loop
                }
            }
        } // bundle null

        if(intent.action == "android.provider.Telephony.SMS_DELIVER"){
            Log.d(TAG, "onReceive: action sms deliver")
        }

    }

    private fun showNotification(context: Context, senderNo: String?, message: String?) {

           DefaultFragmentManager.id = R.id.bottombaritem_messages
           DefaultFragmentManager.defaultFragmentToShow =
               DefaultFragmentManager.SHOW_MESSAGES_FRAGMENT

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(context, IndividualSMSActivity::class.java)
        resultIntent.putExtra(CONTACT_ADDRES, senderNo)
// Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        }


//        // Create an Intent for the activity you want to start
//        val activityIntent = Intent(context, IndividualSMSActivity::class.java)
//        activityIntent.putExtra(CONTACT_ADDRES, senderNo)

// Create the TaskStackBuilder and add the intent, which inflates the back stack
        //This is important because this add the MainActivity to backstack, we have do this because
        //we have set parentActivityName = "MainActivity" for IndividualSMSActivity
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(resultIntent)

        val notification = NotificationCompat
            .Builder(context,HashCaller.CHANNEL_1_ID )
            .setSmallIcon(R.drawable.ic_baseline_textsms_24)
            .setContentTitle(senderNo)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .build()
        //if we use same id it will overrite previous notification
        //if we want to show multiple notification at the same time
        //we have to give different id, if we have to update or cancel a notification
        //we have to pass the same id
        notificationManagerCmpt.notify(1, notification)

    }

    private fun saveSmsInInbox(context: Context, sms: SmsMessage) {
        val serviceIntent = Intent(context, SaveSmsService::class.java)
        serviceIntent.putExtra("sender_no", sms.displayOriginatingAddress)
        serviceIntent.putExtra("message", sms.displayMessageBody)
        serviceIntent.putExtra("date", sms.timestampMillis)
        context.startService(serviceIntent)
    }

    private fun issueNotification(context: Context, senderNo: String, message: String) {
        val icon = BitmapFactory.decodeResource(context.resources,
            R.mipmap.ic_launcher)
        val mBuilder = NotificationCompat.Builder(context)
            .setLargeIcon(icon)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderNo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentText(message)
        val resultIntent = Intent(context, IndividualSMSActivity::class.java)
        resultIntent.putExtra(CONTACT_NAME, senderNo)
        resultIntent.putExtra(FROM_SMS_RECIEVER, true)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mNotificationId = 101
        mNotifyMgr.notify(mNotificationId, mBuilder.build())
    }

    private fun getIncomingMessage(aObject: Any, bundle: Bundle): SmsMessage {
        val currentSMS: SmsMessage
        currentSMS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val format = bundle.getString("format")
            SmsMessage.createFromPdu(aObject as ByteArray, format)
        } else {
            SmsMessage.createFromPdu(aObject as ByteArray)
        }
        return currentSMS
    }
}