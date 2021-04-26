package com.nibble.hashcaller.view.ui.sms.spam

import android.content.Context
import android.provider.CallLog
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.delay

class SpamSMSRepository(private val threadsDAO: ISMSThreadsDAO, private val context: Context) {

    fun getSpamCallLogLivedata(): LiveData<MutableList<SmsThreadTable>> {
       return threadsDAO.getSpamSMSLogLivedata()
    }

    suspend fun markAsDeleted(id: Long) {
        threadsDAO.markAsDeleted(id)

    }

    suspend fun deleteSMSsFromCp(id: Long) {
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