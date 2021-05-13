package com.nibble.hashcaller.view.ui.IncommingCall

import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class LocalDbSearchRepository(private val callersInfoFromServerDAO: CallersInfoFromServerDAO) {


    suspend fun getInfoForNumber(phoneNumber: String): CallersInfoFromServer?  = withContext(Dispatchers.IO){
        delay(1000L)
        return@withContext callersInfoFromServerDAO.find(formatPhoneNumber(phoneNumber))
    }

}