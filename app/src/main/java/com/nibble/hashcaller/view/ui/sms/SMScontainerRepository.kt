package com.nibble.hashcaller.view.ui.sms

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.sms.block.IBlockedOrSpamSendersDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.local.db.sms.mute.MutedSenders
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.contacts.utils.markingStarted
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedContactAddress
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.ui.sms.work.UnknownSMSsendersInfoResponse
import com.nibble.hashcaller.work.formatPhoneNumber
import retrofit2.Response
import java.util.*

class SMScontainerRepository(
    val context: Context,
    val smsSenderInfoDAO: SMSSendersInfoFromServerDAO,
    val mutedSendersDAO: IMutedSendersDAO?,
    val blockedOrSpamSenderDAO: IBlockedOrSpamSendersDAO?,
    private val dataStoreRepostitory: DataStoreRepository,
    private val tokenHelper: TokenHelper?
) {

    private var retrofitService:ISpamService? = null

    /**
     * @return all sms senders numbers list in the localDB which contains 
     * ____________________________________________________
     * contact_address | spammeReportCount | informationRecivedDate | name | type (business or general user) | 
     * -----------------------------------------------------
     * 
     * this is the table schema
     */
    suspend fun geSmsSendersStoredInLocalDB(): List<SMSSendersInfoFromServer> {
       val list =  smsSenderInfoDAO.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownSMSsendersInfoResponse>? {
         retrofitService = RetrofitClient.createaService(ISpamService::class.java)
//        val tokenManager = TokenManager( dataStoreRepostitory )
//        val token = tokenManager.getDecryptedToken()
        val token:String? = tokenHelper?.getToken()
        var response:Response<UnknownSMSsendersInfoResponse>?= null
        token?.let {
            response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        }

        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
        return response
    }

    @SuppressLint("LongLogTag")
    suspend fun deleteSmsThread(): Int {
        var numRowsDeleted = 0
        for(id in markedItems) {
            Log.d(TAG, "deleteSmsThread: threadid $id")
            var uri = Telephony.Sms.CONTENT_URI
            val selection = "${Telephony.Sms.THREAD_ID} = ?"
            val selectionArgs = arrayOf(id.toString())
            try {
                numRowsDeleted = context.contentResolver.delete(uri, selection, selectionArgs)
            } catch (e: Exception) {
                Log.d(TAG, "deleteSmsThread: exception $e")
            }
        }
        deleteList()
        return numRowsDeleted
    }

    private fun deleteList() {
        markedItems.clear()
        markedContactAddress.clear()
        markingStarted = false
    }

    /***
     * function to add contact address to muted_senders table,
     * no notification for incoming sms from muted senders
     */
    suspend fun muteSenders() {
        var addressList: MutableList<MutedSenders> = mutableListOf()
        for (address in MarkedItemsHandler.markedContactAddress){
            val mutedSender = MutedSenders(formatPhoneNumber(address))
            addressList.add(mutedSender)
        }
        mutedSendersDAO!!.insert(addressList)
    }

    /**
     * Adding a new sms sender info who is a spammer
     */
    suspend fun save(contactAddress: String, i: Int, s: String, s1: String) {
        var name = ""
        var spamCount = 0L
       smsSenderInfoDAO.find(formatPhoneNumber(contactAddress)).apply {
           if(this!=null){
               name = this.name
               spamCount = this.spamReportCount

           }
           spamCount+=1
           val info = SMSSendersInfoFromServer(contactAddress, 0,name, Date(), spamCount)
           val list = listOf<SMSSendersInfoFromServer>(info)

           smsSenderInfoDAO!!.insert(list)
           pageOb.page = 0
        }


    }

    suspend fun report(callerInfo: ReportedUserDTo) : Response<NetWorkResponse>? {
        retrofitService = RetrofitClient.createaService(ISpamService::class.java)
       val token:String?= tokenHelper?.getToken()
        var respone:Response<NetWorkResponse>? = null
        respone = token?.let { retrofitService?.report(callerInfo, it) }
        return respone
    }

    companion object{
        const val TAG = "__SMScontainerRepository"
    }

}