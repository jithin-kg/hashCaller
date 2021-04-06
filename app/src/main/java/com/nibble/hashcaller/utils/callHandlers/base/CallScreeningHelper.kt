package com.nibble.hashcaller.utils.callHandlers.base

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.utils.InCommingCallManager
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class CallScreeningHelper(private val context: Context) {

    companion object {
    const val TAG = "__CallScreeningHelper"
    }
    var mutedCallersDAO: IMutedCallersDAO =  context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
    var blockedListpatternDAO: BlockedLIstDao =  context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }

    suspend fun isMutedNumber(phoneNumber: String): Boolean {
        var isMuted = false
        mutedCallersDAO.find(formatPhoneNumber(phoneNumber)).apply {
            if(this!=null){
                isMuted = true
            }
            return isMuted
        }

    }

    /**
     * function to check whether the phoneNumber is followes the pattern that user added to black list
     */
    suspend fun isBlockedByPattern(phoneNumber: String): Boolean {
        var match = false
        GlobalScope.launch {
           async {
             blockedListpatternDAO.getAllBLockListPatternByFlow().collect {
                 for (item in it){
                     if(item.type == NUMBER_STARTS_WITH){
                         match =   phoneNumber.startsWith(item.numberPattern)
                     }else if(item.type == NUMBER_CONTAINING ){
                         match =  phoneNumber.contains(item.numberPattern)
                     }else{
                         match = phoneNumber.endsWith(item.numberPattern)
                     }
                     if(match){

                         break
                     }
                 }
             }

           }.await()
        }.join()
        return match

    }

    }
