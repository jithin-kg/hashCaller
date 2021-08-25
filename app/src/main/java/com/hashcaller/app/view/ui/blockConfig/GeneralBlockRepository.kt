package com.hashcaller.app.view.ui.blockConfig

import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_CONTAINS
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_ENDS_WITH
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_WITH
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.db.ICallLogDAO
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeneralBlockRepository(
    private val callLogDAO: ICallLogDAO?,
    private val smsThreadsDAO: ISMSThreadsDAO?,
    private val blockedLIstDao: BlockedLIstDao?,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryISO: String

) {


    suspend fun marAsReportedByUserInCall(contactAddress: String) = withContext(Dispatchers.IO) {
        val formatedAdders = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryISO)
            callLogDAO?.markAsReportedByUser(formatedAdders, 1)
//        }
    }
    suspend fun markAsNotSpamInCalls(contactAddress: String, color:Int) = withContext(Dispatchers.IO) {
        val formatedAdders = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryISO)
        callLogDAO?.removeFromBlockList(formatedAdders, color = color)
    }


    suspend fun marAsReportedByUserInSMS(contactAddress: String) = withContext(Dispatchers.IO){
        var formatedAddress = formatPhoneNumber(contactAddress)
        formatedAddress = libPhoneCodeHelper.getES164Formatednumber(formatedAddress, countryISO)
        val res  = smsThreadsDAO?.find(formatedAddress)
        if(res!=null){
            var spamCount = res.spamCount
            spamCount+=1
            smsThreadsDAO?.updateSpamCount(formatedAddress, spamCount = spamCount )


        }
    }
    suspend fun markAsNotSpamInSMS(phoneNum: String, randomColor: Int) = withContext(Dispatchers.IO) {
        val formatedAddress = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(phoneNum), countryISO)
        smsThreadsDAO?.markAsNotSpam(formatedAddress )
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