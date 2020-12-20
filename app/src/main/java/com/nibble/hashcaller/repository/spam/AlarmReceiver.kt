package com.nibble.hashcaller.repository.spam

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity

/**
 * Reciever that deletes spam sms for a set time by user
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManagerCmpt = NotificationManagerCompat.from(context!!)

        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra("delete", "delete spam")

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context!!).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat
            .Builder(context!!, HashCaller.CHANNEL_1_ID )
            .setSmallIcon(R.drawable.ic_baseline_textsms_24)
            .setContentTitle("Do you want to delete spam messages")
            .setContentText("click to review the spam messages before deleting")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManagerCmpt.notify(1, notification)
    }

}