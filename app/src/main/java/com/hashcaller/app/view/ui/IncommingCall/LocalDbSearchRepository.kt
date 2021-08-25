package com.hashcaller.app.view.ui.IncommingCall

import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDbSearchRepository(
    private val callersInfoFromServerDAO: CallersInfoFromServerDAO,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryISO: String,

    ) {


    suspend fun getInfoForNumber(phoneNumber: String): CallersInfoFromServer?  = withContext(Dispatchers.IO){
//        delay(1000L)
        val formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(phoneNumber), countryISO)
        return@withContext callersInfoFromServerDAO.find(formatedNum)
    }

}