package com.nibble.hashcaller.view.ui.call.spam

import androidx.lifecycle.LiveData
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO

class SpamCallRepository(private val callLogDao: ICallLogDAO) {
    fun getCallLogLivedata(): LiveData<MutableList<CallLogTable>> {
       return callLogDao.getSpamCallLogLivedata()
    }

}