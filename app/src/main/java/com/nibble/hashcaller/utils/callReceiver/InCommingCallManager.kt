package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.contacts.IContactAddressesDao
import com.nibble.hashcaller.network.StatusCodes.Companion.STATUS_OK
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

/**
 * Created by Jithin KG on 20,July,2020
 */
class InCommingCallManager(
    private val context: Context,
    num: String,
    private val blockNonContactsEnabled: Boolean,
    private val notificationHelper: NotificationHelper?,
    private val searchRepository: SearchNetworkRepository,
    private val internetChecker: InternetChecker,
    private val blockedListpatternDAO: BlockedLIstDao,
    private val contactAdressesDAO: IContactAddressesDao,
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO
)  {
    private val phoneNumber = formatPhoneNumber(num)


    suspend fun searchInServerAndHandle(hasedNum: String): CntctitemForView {
        var searchResult = CntctitemForView(  "", "", "", "", "", "", 0)
        try {
                val response = searchRepository.search(hasedNum)

                    val result = response?.body()?.cntcts
                    if(result!= null){
                        searchResult = CntctitemForView(result.firstName?:"", result.lastName?:"", result.carrier?:"",
                            result.location?:"", result.lineType?:"",
                            result.country?:"",
                        result.spammCount?:0L,
                                    response.body()?.status?:0)
                    }

        }catch (e:Exception){
            Log.d(TAG, "searchInServerAndHandle:exception  $e")
        }
        return searchResult

    }


    private suspend fun blockByPattern(phoneNumber: String) {
        var match: Boolean
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
                    endIncommingCall()
                    break
                }
            }
        }
    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
    }


     fun endIncommingCall() {
         Log.d(TAG, "endIncommingCall: ")
        val c = CallEnder(context)
        c.endIncomingCall()
    }

    /**
     * increment the total number of calls blocked by hash caller in server
     * for analytics
     */
    @SuppressLint("LongLogTag")
    private suspend fun incrementTotalSpamCountByHashCallerInServer(
        searchRepository: SearchNetworkRepository
    ) {
        Log.d(TAG +"increment", "incrementTotalSpamCountByHashCallerInServer: ")
        searchRepository.incrementTotalSpamCount()
    }

    fun silenceIncomingCall(context: Context){
        val c = CallEnder(context)
        c.silenceIncomingCall()
    }

    suspend fun isBlockedByPattern(): Boolean {
        var match = false
      for (item in blockedListpatternDAO.getAllBLockListPatternList()){

              if(item.type == NUMBER_STARTS_WITH){
                  match =   phoneNumber.startsWith(item.numberPattern)
              }else if(item.type == NUMBER_CONTAINING ){
                  match =  phoneNumber.contains(item.numberPattern)
              }else{
                  match = phoneNumber.endsWith(item.numberPattern)
              }
              if(match){
//                  endIncommingCall(context)
                    break
              }
             }
        return match

    }

    /**
     * if the function returns true block the call
     */
    suspend fun isNonContactsCallsAllowed(): Boolean {
        var isBlock  = false
        val res = contactAdressesDAO.find(phoneNumber)
            if (res == null) {
                //this number not in contacts
                if (blockNonContactsEnabled) {
                    isBlock = true
//                    endIncommingCall(context)
//                    notificationHelper.showNotificatification(true, phoneNumber)
                }
            }
        return isBlock

    }

    suspend fun getAvailbleInfoInDb(): CntctitemForView? = withContext(Dispatchers.IO) {
        var contactitemForView : CntctitemForView? = null
        val res = callerInfoFromServerDAO.find(phoneNumber)
        if(res!=null){
            contactitemForView = CntctitemForView(
                firstName = res.title,
                carrier = res.carrier,
                location = res.city,
                country = res.country,
                spammCount = res.spamReportCount,
                statusCode = STATUS_OK
            )

        }
        return@withContext contactitemForView
    }

    fun infoFromContentProvider() {

    }


}