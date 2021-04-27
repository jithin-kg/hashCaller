package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialerRepository(private val context: Context, private val callLogDAO: ICallLogDAO?) {
     suspend fun getFirst10Logs(): MutableList<CallLogTable>? = withContext(Dispatchers.IO)  {
         return@withContext callLogDAO?.getFirst10Logs()
    }


}