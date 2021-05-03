package com.nibble.hashcaller.utils.callHandlers.base

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.contacts.IContactAddressesDao
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CallScreeningHelper(private val context: Context, private val contactAdressesDAO: IContactAddressesDao) {

    companion object {
    const val TAG = "__CallScreeningHelper"
    }
    var mutedCallersDAO: IMutedCallersDAO =  context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
    var blockedListpatternDAO: BlockedLIstDao =  context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }



    suspend fun isMutedNumber(phoneNumber: String): Boolean  = withContext(Dispatchers.IO) {
        var isMuted = false
        val res = mutedCallersDAO.find(formatPhoneNumber(phoneNumber))
            if(res!=null){
                isMuted = true
            }
        return@withContext isMuted


    }

    /**
     * function to check whether the phoneNumber is followes the pattern that user added to black list
     */
    suspend fun isBlockedByPattern(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        var match = false


             val res =  blockedListpatternDAO.getAllBLockListPatternList()
                for (item in res){
                    Log.d(TAG, "isBlockedByPattern: ${item.numberPattern}")
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


        return@withContext match

    }

    /**
     * function returns true if the user enabled block non contacts call
     */
    suspend fun isThisCallTobeBlocked(formatedNum: String, blockNonContactsEnabled: Boolean):Boolean  = withContext(Dispatchers.IO){
        val res = contactAdressesDAO.find(formatedNum)
            if(res == null){
                if(blockNonContactsEnabled){
                    return@withContext true
                }
            }

        return@withContext false
    }

}
