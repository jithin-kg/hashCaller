package com.hashcaller.app.utils.callReceiver

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_CONTAINS
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_WITH
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.local.db.contacts.IContactAddressesDao
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.network.search.model.DTOMapper
import com.hashcaller.app.network.search.model.DTOMapper.Companion.convertServerResToContactView
import com.hashcaller.app.network.search.model.DTOMapper.Companion.serverResultToConctView
import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.NotificationHelper
import com.hashcaller.app.utils.getStringValue
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.contacts.showNotifcationForSpamCall
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.*

/**
 * Created by Jithin KG on 20,July,2020
 */

class InCommingCallManager(
    private val context: Context,
    num: String,
    private val notificationHelper: NotificationHelper?,
    private val searchRepository: SearchNetworkRepository,
    private val internetChecker: InternetChecker,
    private val blockedListpatternDAO: BlockedLIstDao,
    private val contactAdressesDAO: IContactAddressesDao,
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO,
    private val countryCodeIso: String,
    private val spamThreshold: Int
)  {
    private val phoneNumber = formatPhoneNumber(num)
    private val libPhoneCodeHelper =  LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private val countryIso = CountrycodeHelper(context).getCountryISO()

//    suspend fun searchInserver(hashedNum:String): Response<SerachRes>? {
//        var response : Response<SerachRes>? = null
//        try {
//             response = searchRepository.search(hashedNum)
//        }catch (e:Exception){
//            Log.d(TAG, "searchInserver: $e")
//        }
//        return response
//    }
    suspend fun searchInServerAndHandle(hasedNum: String): CntctitemForView? {
        var searchResult:CntctitemForView? = null

        try {
                val response = searchRepository.search(hasedNum)

                    val result = response?.body()?.cntcts

                    if(result!= null){
                        searchResult = serverResultToConctView(response)
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
                if(item.type == BLOCK_TYPE_STARTS_WITH){
                    match =   phoneNumber.startsWith(item.numberPattern)
                }else if(item.type == BLOCK_TYPE_CONTAINS ){
                    match =  phoneNumber.contains(item.numberPattern)
                }else{
                    match = phoneNumber.endsWith(item.numberPattern)
                }
                if(match){
                    endIncommingCall(REASON_BLOCK_BY_PATTERN)
                    break
                }
            }
        }
    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
        const val REASON_BLOCK_NON_CONTACT = 1
        const val REASON_BLOCK_TOP_SPAMMER = 2
        const val REASON_BLOCK_BY_PATTERN = 3
//        const val REASON_BLOCK_FOREIGN_NUMBER= 4
        const val REASON_FOREIGN = 9
    }


     suspend fun endIncommingCall(reason: Int) {
         Log.d(TAG, "endIncommingCall: $reason ")
        val c = CallEnder(context)
        if(c.endIncomingCall()){
            //call ended successfully, show notificatoin
            context.showNotifcationForSpamCall(reason, phoneNumber)
        }
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

              if(item.type == BLOCK_TYPE_STARTS_WITH) {
                  match =   phoneNumber.startsWith(item.numberPattern)
              }else if(item.type == BLOCK_TYPE_CONTAINS ){
                  match =  phoneNumber.contains(item.numberPattern)
              }else {
                  match = phoneNumber.endsWith(item.numberPattern)
              }
              if(match){
//                  endIncommingCall(context)
                    break
              }
             }
        return match

    }

    suspend fun isBlockForeignCountryEnabled(): Boolean {
        if(isBlockEnabledForKey(PreferencesKeys.KEY_BLOCK_FOREIGN_NUMBER)){
            if(libPhoneCodeHelper.getCountryIso(phoneNumber, countryIso) != countryIso){
               return  true
            }
        }
        return false
    }
    /**
     * if the function returns true block the call
     */
    suspend fun isNonContactsCallsAllowed(): Boolean {
        var isBlock  = false
        if(isBlockEnabledForKey(PreferencesKeys.KEY_BLOCK_NON_CONTACT)){
            val res = getContactDetailForNumberFromCp(phoneNumber)
            if (res == null) {
                //this number not in contacts
                isBlock = true

//                    endIncommingCall(context)
//                    notificationHelper?.showNotificatification(true,
//                        phoneNumber,
//                        "Call from $phoneNumber blocked because you enabled block calls from persosn not in contacts"
//                        )
            }
        }


        return isBlock

    }

    private suspend fun isBlockEnabledForKey(key: String): Boolean {
        val wrapedKey =  booleanPreferencesKey(key)
        val tokenFlow: Flow<Boolean> = context.tokeDataStore.data.map {
            it[wrapedKey]?:false
        }
        return tokenFlow.first()
    }

    suspend fun getAvailbleInfoInDb(): CntctitemForView? = withContext(Dispatchers.IO) {
        var contactitemForView : CntctitemForView? = null
        var formatedNum  =formatPhoneNumber(phoneNumber)

        val res = callerInfoFromServerDAO.find(formatPhoneNumber(phoneNumber))
        if(res!=null){
            if(res.firstName.isNotEmpty() || res.nameInPhoneBook.isNotEmpty() || res.spamReportCount > spamThreshold){
                contactitemForView = convertServerResToContactView(res)
            }


        }
        return@withContext contactitemForView
    }

    suspend fun infoFromContentProvider(): Contact?  = withContext(Dispatchers.IO){
        var contact:Contact? = null
        var cursor: Cursor? = null
        var name:String? = phoneNumber
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
                    val id:String? = cursor.getStringValue(ContactsContract.PhoneLookup.CONTACT_ID)
                    name=  cursor.getStringValue(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val thumbnail:String? = cursor.getStringValue(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                   contact = id?.let { it1 -> Contact(it1.toLong(), nameInLocalPhoneBook = name?:phoneNumber, thumbnailInCprovider =thumbnail?:"") }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getNameFromPhoneNumber: $e")
        }
        finally {
            cursor?.close()
        }
        return@withContext contact
    }

    suspend fun saveInfoFromServer(resFromServer: CntctitemForView?, phoneNumber: String) = withContext(Dispatchers.IO) {
        var formatedNum = formatPhoneNumber(phoneNumber)
        formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatedNum, countryIso)
        val res = callerInfoFromServerDAO.find(formatedNum)
        if(res== null){

            val info = DTOMapper.cntctitemForViewTOCallersInfoFromServer(resFromServer, formatedNum)

            callerInfoFromServerDAO.insert(listOf(info))
        }else{
            //todo add client hashed phone number also
            callerInfoFromServerDAO?.updateWithServerinfo(
                contactAddress = formatedNum,
                firstName = resFromServer?.firstName?:"",
                lastName = resFromServer?.lastName?:"",
                nameInPhoneBook = resFromServer?.nameInPhoneBook?:"",
                informationReceivedDate = Date(),
                spamReportCount = resFromServer?.spammCount?:0L,
                city = resFromServer?.location?:"",
                country = resFromServer?.country?:"",
                carrier = resFromServer?.carrier?:"",
                isUserInfoFoundInServer = resFromServer?.isInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER,
                spammerType = resFromServer?.spammerType?:0 ,
                thumbnailImg = resFromServer?.thumbnailImgServer?:"",
                hUid = resFromServer?.hUid?:"",
                bio = resFromServer?.bio?:"",
                email = resFromServer?.email?:"",
                avatarGoogle = resFromServer?.avatarGoogle?:"",
                isVerifiedUser = resFromServer?.isVerifiedUser?:false
                )
        }
    }

    suspend fun getContactDetailForNumberFromCp(phoneNumber: String): Contact?  = withContext(Dispatchers.IO) {
        var cursor:Cursor? = null
        val phoneNum = phoneNumber.replace("+", "").trim()
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )

        var  contact:Contact? = null
        try{
            if(cursor2!=null && cursor2.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
                val name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
                val contactId = cursor2.getLong(cursor2.getColumnIndex("contact_id"))
                val normalizedNumber = cursor2.getString(cursor2.getColumnIndex("normalized_number"))
                contact = Contact(contactId, name, normalizedNumber, null)
            }


        }catch (e:Exception){
            Log.d(TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }
        return@withContext contact
    }




}