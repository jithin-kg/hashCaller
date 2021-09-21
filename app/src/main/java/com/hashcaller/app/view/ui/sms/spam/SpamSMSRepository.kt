package com.hashcaller.app.view.ui.sms.spam

import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpamSMSRepository(private val threadsDAO: ISMSThreadsDAO, private val context: Context) {

    fun getSpamCallLogLivedata(): LiveData<MutableList<SmsThreadTable>> {
       return threadsDAO.getSpamSMSLogLivedata(spsmCountLimit=15L)
    }

    suspend fun markAsDeleted(id: Long)  = withContext(Dispatchers.IO){
        threadsDAO.markAsDeleted(id)

    }

    suspend fun deleteSMSsFromCp(id: Long) = withContext(Dispatchers.IO) {
        try{
            var uri = Telephony.Sms.CONTENT_URI
            val selection = "${Telephony.Sms.THREAD_ID} = ?"
            val selectionArgs = arrayOf(id.toString())
            val numRowsDeleted = context.contentResolver.delete(uri, selection, selectionArgs)

//            }
        }catch (e: Exception) {
            Log.d(TAG, "deleteSmsThread: exception $e")
        }

    }

    companion object{
        const val TAG = "__SpamSMSRepository"
    }

}