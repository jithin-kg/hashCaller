package com.hashcaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")
//        val address = intent.getStringExtra(THREAD_NUMBER)
//        val threadId = intent.getLongExtra(THREAD_ID, 0L)
//        val msg = RemoteInput.getResultsFromIntent(intent).getCharSequence(REPLY).toString()
//
//        val settings = Settings()
//        settings.useSystemSending = true
//
//        val transaction = Transaction(context, settings)
//        val message = com.klinker.android.send_message.Message(msg, address)
//
//        try {
//            transaction.sendNewMessage(message, threadId)
//        } catch (e: Exception) {
//            context.showErrorToast(e)
//        }
//
//        val repliedNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
//            .setSmallIcon(R.drawable.ic_messenger)
//            .setContentText(msg)
//            .build()
//
//        context.notificationManager.notify(threadId.hashCode(), repliedNotification)
//
//        ensureBackgroundThread {
//            context.markThreadMessagesRead(threadId)
//            context.conversationsDB.markRead(threadId)
//        }
    }
    companion object{
        const val TAG ="__DirectReplyReceiver"
    }
}