package com.hashcaller.app.view.ui.call.spam

import android.content.Context
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.db.ICallLogDAO
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpamCallRepository(
    private val callLogDao: ICallLogDAO,
    private val context: Context,
    private val spamThreshold: Int,
    ) {
    fun getSpamCallLogLivedata(): LiveData<MutableList<CallLogTable>> {
       return callLogDao.getSpamCallLogLivedata(spamLimit = spamThreshold.toLong())
    }

    suspend fun markAsDeleted(num: String) = withContext(Dispatchers.IO) {
        callLogDao.markAsDeleted(formatPhoneNumber(num))

    }


    suspend fun deleteCallLogsFromCp(number: String) = withContext(Dispatchers.IO) {
        try {
            var uri = CallLog.Calls.CONTENT_URI
            val selection = "${CallLog.Calls.NUMBER} = ?"
//                callLogDAO?.delete(id)
            val selectionArgs = arrayOf(number)
            context.contentResolver.delete(uri, selection, selectionArgs)

        }catch (e: Exception) {
            Log.d(TAG, "deleteSmsThread: exception $e")
        }

    }

    companion object{
        const val TAG = "__SpamCallRepository"
    }

}