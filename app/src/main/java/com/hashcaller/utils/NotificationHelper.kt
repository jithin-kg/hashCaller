package com.hashcaller.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.hashcaller.R
import com.hashcaller.utils.notifications.HashCaller
import com.hashcaller.view.ui.MainActivity

class NotificationHelper(
    private val isReceiveNotificationForSpamCallBlk: Boolean,
    context: Context
//    private val resultIntent: Intent,
//    private val notificationCmpt: NotificationCompat.Builder,
//    private val resultPendingIntent: PendingIntent?,
//    private val notificationManagerCmpt: NotificationManagerCompat,

    ) {

    var notificationCmpt =   NotificationCompat.Builder(context, HashCaller.CHANNEL_2_ID)
    val  resultIntent= Intent(context, MainActivity::class.java)
    var notificationManagerCmpt: NotificationManagerCompat = NotificationManagerCompat.from(context)

    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        // Add the intent, which inflates the back stack
        addNextIntentWithParentStack(resultIntent)
        // Get the PendingIntent containing the entire back stack
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * funcftion to handle notificatoin, if call blocked and user preference is to
     * to show notify for blocked calls , then show notification
     */
    @SuppressLint("LongLogTag")
     fun showNotificatification(isBlocked: Boolean, phoneNumber: String, content: String) {
//        var notificationManagerCmpt: NotificationManagerCompat = NotificationManagerCompat.from(this)
        if(isBlocked && isReceiveNotificationForSpamCallBlk){
            Log.d(TAG, "showNotificatification: ")
//            resultIntent.putExtra(CONTACT_ADDRES, senderNo)

// Create the TaskStackBuilder

            val notification = notificationCmpt
                .setSmallIcon(R.drawable.ic_baseline_block_red)
                .setContentTitle("Call Blocked")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManagerCmpt.notify(2, notification)

        }
    }

    fun showNotificationForgroundCallService(phoneNumber: String) {
        val notification = notificationCmpt
            .setSmallIcon(R.drawable.ic_baseline_block_red)
            .setContentTitle("Call from $phoneNumber")
            .setContentText("Caller id is active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManagerCmpt.notify(2, notification)
    }

    companion object {
        const val TAG = "__NotificationHelper"
    }
}