package com.nibble.hashcaller.view.ui.blockConfig

import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeneralBlockRepository(
    private val callLogDAO: ICallLogDAO,
    private val smsThreadsDAO: ISMSThreadsDAO?,

    ) {


    suspend fun marAsReportedByUserInCall(contactAddress: String) {
        val formatedAdders = formatPhoneNumber(contactAddress)
        val log =  callLogDAO?.findOne(formatedAdders)
        if(log!=null){
            var spamCount = log.spamCount
            spamCount += 1
            callLogDAO?.markAsReportedByUser(formatedAdders, spamCount)
        }
    }

    suspend fun marAsReportedByUserInSMS(contactAddress: String) = withContext(Dispatchers.IO){
        val formatedAddress = formatPhoneNumber(contactAddress)
        smsThreadsDAO?.updateSpamCount(formatedAddress,  true)
    }

    companion object{
        const val TAG = "__GeneralBlockRepository"
    }
}