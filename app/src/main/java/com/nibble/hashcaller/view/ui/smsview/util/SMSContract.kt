package com.nibble.hashcaller.view.ui.smsview.util

import android.net.Uri

object SMSContract {
    @JvmField
    val ALL_SMS_URI = Uri.parse("content://sms/inbox")
    const val SMS_SELECTION = "address = ? "
    const val SMS_SELECTION_ID = "_id = ? "
    const val COLUMN_ID = "_id"
    const val SMS_SELECTION_SEARCH = "address LIKE ? OR body LIKE ?"
    const val SORT_DESC = "date DESC"
    const val SORT_ASC = "date ASC"
}