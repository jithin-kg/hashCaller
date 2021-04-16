package com.nibble.hashcaller.view.ui.call.search

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.repository.CallLocalRepository
import com.nibble.hashcaller.work.formatPhoneNumber
import java.text.SimpleDateFormat

class CallLogSearchRepository(
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO,
    private val callLogDAO: ICallLogDAO
) {
    suspend fun search(text: String): MutableList<CallLogTable> {
        callLogDAO?.searchCalllog("%$text%").apply {

           return this
       }
    }
}