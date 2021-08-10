package com.hashcaller.utils

import android.content.Context
import android.net.Uri
import android.util.Log

// more info at https://github.com/klinker41/android-smsmms
class MmsReceiver : com.klinker.android.send_message.MmsReceivedReceiver() {
    companion object{
        const val TAG = "__MmsReceiver"
    }
    override fun onMessageReceived(context: Context, messageUri: Uri) {
        Log.d(TAG, "onMessageReceived: ")
//        val mms = context.getLatestMMS() ?: return
//        val address = mms.participants.firstOrNull()?.phoneNumbers?.first() ?: ""
//        if (context.isNumberBlocked(address)) {
//            return
//        }
//
//        val size = context.resources.getDimension(R.dimen.notification_large_icon_size).toInt()
//        ensureBackgroundThread {
//            val glideBitmap = try {
//                Glide.with(context)
//                    .asBitmap()
//                    .load(mms.attachment!!.attachments.first().getUri())
//                    .centerCrop()
//                    .into(size, size)
//                    .get()
//            } catch (e: Exception) {
//                null
//            }
//
//            Handler(Looper.getMainLooper()).post {
//                context.showReceivedMessageNotification(address, mms.body, mms.threadId, glideBitmap)
//                val conversation = context.getConversations(mms.threadId).firstOrNull() ?: return@post
//                ensureBackgroundThread {
//                    context.conversationsDB.insertOrUpdate(conversation)
//                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
//                }
//            }
//        }
    }

    override fun onError(context: Context, error: String) {}
}