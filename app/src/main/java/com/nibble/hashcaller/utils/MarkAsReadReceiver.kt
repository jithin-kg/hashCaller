package com.nibble.hashcaller.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MarkAsReadReceiver : BroadcastReceiver() {
    companion object{
        const val TAG = "__MarkAsReadReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")
        when (intent.action) {
//            MARK_AS_READ -> {
//                val threadId = intent.getLongExtra(THREAD_ID, 0L)
//                context.notificationManager.cancel(threadId.hashCode())
//                ensureBackgroundThread {
//                    context.markThreadMessagesRead(threadId)
//                    context.conversationsDB.markRead(threadId)
//                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
//                }
//            }
        }
    }
}