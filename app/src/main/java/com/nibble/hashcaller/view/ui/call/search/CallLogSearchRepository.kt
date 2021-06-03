package com.nibble.hashcaller.view.ui.call.search

import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO

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