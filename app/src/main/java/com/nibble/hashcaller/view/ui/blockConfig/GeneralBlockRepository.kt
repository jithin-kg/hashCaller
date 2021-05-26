package com.nibble.hashcaller.view.ui.blockConfig

import com.nibble.hashcaller.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.nibble.hashcaller.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_CONTAINS
import com.nibble.hashcaller.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_ENDS_WITH
import com.nibble.hashcaller.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_WITH
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeneralBlockRepository(
    private val callLogDAO: ICallLogDAO?,
    private val smsThreadsDAO: ISMSThreadsDAO?,
    private val blockedLIstDao: BlockedLIstDao?

    ) {


    suspend fun marAsReportedByUserInCall(contactAddress: String) = withContext(Dispatchers.IO) {
        val formatedAdders = formatPhoneNumber(contactAddress)
//        val log =  callLogDAO?.findOne(formatedAdders)
//        if(log!=null){
//            var spamCount = log.spamCount
//            spamCount += 1
            callLogDAO?.markAsReportedByUser(formatedAdders, 1)
//        }
    }

    suspend fun marAsReportedByUserInSMS(contactAddress: String) = withContext(Dispatchers.IO){
        val formatedAddress = formatPhoneNumber(contactAddress)
        val res  = smsThreadsDAO?.find(formatedAddress)
        if(res!=null){
            var spamCount = res.spamCount
            spamCount+=1
            smsThreadsDAO?.updateSpamCount(formatedAddress, spamCount = spamCount )


        }
    }

    suspend fun updateCallLogsWithblockListpatterns(logs: MutableList<CallLogTable>) = withContext(Dispatchers.IO) {
        val patterns = blockedLIstDao?.getAllBLockListPatternList()
        if (patterns != null) {
            for(pattern in patterns){
                var macheditem:CallLogTable? = null
                when(pattern.type){
                    BLOCK_TYPE_EXACT_NUMBER ->{
                       macheditem =   logs.find { it.numberFormated == pattern.numberPattern }
                    }
                    BLOCK_TYPE_STARTS_WITH ->{

                    }
                    BLOCK_TYPE_STARTS_CONTAINS ->{

                    }
                    BLOCK_TYPE_STARTS_ENDS_WITH ->{

                    }

                }
                if(macheditem!=null){
                    callLogDAO?.markAsReportedByUser(macheditem.numberFormated,1 )
                }
            }
        }
    }

    suspend fun updateSMSWithBlockListPattern(smsThreads: MutableList<SmsThreadTable>) = withContext(Dispatchers.IO) {
        val patterns = blockedLIstDao?.getAllBLockListPatternList()
        if (patterns != null) {
            for(pattern in patterns){
                var macheditem:SmsThreadTable? = null
                when(pattern.type){
                    BLOCK_TYPE_EXACT_NUMBER ->{
                        macheditem =   smsThreads.find { it.numFormated == pattern.numberPattern }
                    }
                    BLOCK_TYPE_STARTS_WITH ->{

                    }
                    BLOCK_TYPE_STARTS_CONTAINS ->{

                    }
                    BLOCK_TYPE_STARTS_ENDS_WITH ->{

                    }

                }
                if(macheditem!=null){
                    val res  = smsThreadsDAO?.find(macheditem.numFormated)
                    if(res!=null){
                        var spamCount = res.spamCount
                        spamCount+=1
                        smsThreadsDAO?.updateSpamCount(macheditem.numFormated, spamCount = spamCount )
                    }
                }
            }
        }
    }

    companion object{
        const val TAG = "__GeneralBlockRepository"
    }
}