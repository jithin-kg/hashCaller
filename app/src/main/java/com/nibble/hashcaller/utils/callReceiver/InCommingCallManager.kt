package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.contacts.IContactAddressesDao
import com.nibble.hashcaller.network.StatusCodes.Companion.STATUS_OK
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.getStringValue
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.*

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


    suspend fun searchInServerAndHandle(hasedNum: String): CntctitemForView? {
        var searchResult:CntctitemForView? = null

        try {
                val response = searchRepository.search(hasedNum)

                    val result = response?.body()?.cntcts
                    if(result!= null){
                        searchResult = CntctitemForView(result.firstName?:"", result.lastName?:"", result.carrier?:"",
                            location = result.location?:"", result.lineType?:"",
                            country = result.country?:"",
                            spammCount = result.spammCount?:0L,
                                thumbnailImg = result.thumbnailImg?:"",
                                    statusCode = response.body()?.status?:0,
                                        isInfoFoundInServer = result.isInfoFoundInDb?:0,
                                    informationReceivedDate = Date()

                        )
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
        val res = callerInfoFromServerDAO.find(formatPhoneNumber(phoneNumber))
        if(res!=null){
            contactitemForView = CntctitemForView(
                firstName = res.firstName,
                lastName = res.lastName,
                carrier = res.carrier,
                location = res.city,
                country = res.country,
                spammCount = res.spamReportCount,
                thumbnailImg = res.thumbnailImg,
                statusCode = STATUS_OK,
                informationReceivedDate = Date()
            )

        }
        return@withContext contactitemForView
    }

    suspend fun infoFromContentProvider(): Contact?  = withContext(Dispatchers.IO){

        var contact:Contact? = null
        var cursor: Cursor? = null
        var name = phoneNumber
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(
            ContactsContract.PhoneLookup.CONTACT_ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        )
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    //this table contains, stared contacts and other usefull informations
                    val id = cursor.getStringValue(ContactsContract.PhoneLookup.CONTACT_ID)
                    name=  cursor.getStringValue(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val thumbnail = cursor.getStringValue(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                   contact =  Contact(id.toLong(), firstName = name, photoThumnailServer =thumbnail)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getNameFromPhoneNumber: $e")
        }
        finally {
            cursor?.close()
        }
        Log.d(TAG, "infoFromContentProvider:name is  $name ")
        return@withContext contact
    }

    suspend fun saveInfoFromServer(resFromServer: CntctitemForView?, phoneNumber: String) = withContext(Dispatchers.IO) {
        val formatedNum = formatPhoneNumber(phoneNumber)

        val res = callerInfoFromServerDAO.find(formatedNum)
        if(res== null){
            val info = CallersInfoFromServer(
                contactAddress = formatedNum,
                spammerType = 0,
                firstName = resFromServer?.firstName?:"",
                lastName = resFromServer?.lastName?:"",
                 informationReceivedDate =         Date(),
                spamReportCount = resFromServer?.spammCount?:0L,
                city = resFromServer?.location?:"",
                country = resFromServer?.country?:"",
                carrier = resFromServer?.carrier?:"",
                isBlockedByUser = false,
            isUserInfoFoundInServer = resFromServer?.isInfoFoundInServer?:0,
                thumbnailImg = resFromServer?.thumbnailImg?:""
                )

            callerInfoFromServerDAO.insert(listOf(info))
        }else{
            callerInfoFromServerDAO?.updateWithServerinfo(
                 contactAddress = formatedNum,
                firstName = resFromServer?.firstName?:"",
                lastName = resFromServer?.lastName?:"",
                informationReceivedDate = Date(),
                spamReportCount = resFromServer?.spammCount?:0L,
                city = resFromServer?.location?:"",
                country = resFromServer?.country?:"",
                carrier = resFromServer?.carrier?:""
                )
        }
    }


}