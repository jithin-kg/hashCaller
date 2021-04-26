package com.nibble.hashcaller.view.ui.call.spam

import android.content.Context
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO

class SpamCallRepository(private val callLogDao: ICallLogDAO, private val context: Context) {
    fun getSpamCallLogLivedata(): LiveData<MutableList<CallLogTable>> {
       return callLogDao.getSpamCallLogLivedata()
    }

    suspend fun deleteById(id: Long) {
        callLogDao.delete(id)

    }

    suspend fun deleteCallLogsFromCp(number: String) {
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