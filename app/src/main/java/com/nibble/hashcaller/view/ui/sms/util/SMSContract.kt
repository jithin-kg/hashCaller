package com.nibble.hashcaller.view.ui.sms.util

import android.net.Uri

object SMSContract {
    @JvmField
    val INBOX_SMS_URI = Uri.parse("content://sms/inbox")
    val SMS_OUTBOX_URI =  Uri.parse("content://sms/outbox")

    val ALL_SMS_URI = Uri.parse("content://sms/");

    const val SMS_SELECTION = "address = ? "
    const val SMS_SELECTION_ID = "_id = ? "
    const val COLUMN_ID = "_id"
    const val SMS_SELECTION_SEARCH = "address LIKE ? OR body LIKE ?"
    const val SORT_DESC = "date DESC"
    const val SORT_ASC = "date ASC"
}