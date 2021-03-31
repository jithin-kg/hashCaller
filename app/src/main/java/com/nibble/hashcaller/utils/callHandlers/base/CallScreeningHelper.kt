package com.nibble.hashcaller.utils.callHandlers.base

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.work.formatPhoneNumber


class CallScreeningHelper(private val context: Context) {
    var mutedCallersDAO: IMutedCallersDAO =  context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }

    suspend fun isMutedNumber(phoneNumber: String): Boolean {
        var isMuted = false
        mutedCallersDAO.find(formatPhoneNumber(phoneNumber)).apply {
            if(this!=null){
                isMuted = true
            }
            return isMuted
        }

    }


}