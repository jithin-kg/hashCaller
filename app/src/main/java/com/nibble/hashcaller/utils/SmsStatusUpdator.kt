package com.nibble.hashcaller.utils

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony

object SmsStatusUpdator {
    fun updateMessageType(context: Context, id:Long, status:Int) {
        val uri = Telephony.Sms.CONTENT_URI
        val contentValues = ContentValues().apply {
            put(Telephony.Sms.TYPE, status)
        }
        val selection = "${Telephony.Sms._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        context.contentResolver.update(uri, contentValues, selection, selectionArgs)
    }
}