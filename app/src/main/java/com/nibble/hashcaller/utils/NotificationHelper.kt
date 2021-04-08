package com.nibble.hashcaller.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nibble.hashcaller.R

class NotificationHelper(
    private val isReceiveNotificationForSpamCallBlk: Boolean,
    private val resultIntent: Intent,
    private val notificationCmpt: NotificationCompat.Builder,
    private val resultPendingIntent: PendingIntent?,
    private val notificationManagerCmpt: NotificationManagerCompat,

    ) {

    /**
     * funcftion to handle notificatoin, if call blocked and user preference is to
     * to show notify for blocked calls , then show notification
     */
    @SuppressLint("LongLogTag")
     fun showNotificatification(isBlocked: Boolean, phoneNumber: String) {
//        var notificationManagerCmpt: NotificationManagerCompat = NotificationManagerCompat.from(this)
        if(isBlocked && isReceiveNotificationForSpamCallBlk){

//            resultIntent.putExtra(CONTACT_ADDRES, senderNo)

// Create the TaskStackBuilder

            val notification = notificationCmpt
                .setSmallIcon(R.drawable.ic_baseline_block_red)
                .setContentTitle("Call Blocked")
                .setContentText("Call from $phoneNumber is blocked")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManagerCmpt.notify(2, notification)

        }
    }
}