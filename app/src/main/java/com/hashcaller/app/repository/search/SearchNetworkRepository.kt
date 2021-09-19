package com.hashcaller.app.repository.search

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.search.ISearchService
import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.search.ServerSearchViewModel
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception
import java.util.*

class SearchNetworkRepository(
    private val tokenHelper: TokenHelper?,
    private val callersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryIso: String
){

    private var retrofitService:ISearchService?  = RetrofitClient.createaService(ISearchService::class.java)
    @SuppressLint("LongLogTag")

    //todo https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // i should user a refresh token

    suspend fun search(phoneNum: String): Response<SerachRes>?  = withContext(Dispatchers.IO){
        var result : Response<SerachRes>? = null
        try {
//            val token = tokenManager.getDecryptedToken()
           val token =  tokenHelper?.getToken()
            if(!token.isNullOrEmpty()){
                    result =  retrofitService?.search(SearchDTO(phoneNum), token)
                    Log.d(TAG, "search: $result")
                }


        }catch (e:Exception){
            Log.d(TAG, "search:exception $e")
        }

        return@withContext result
    }

    /**
     * function to call when user perform searching from view
     */
    suspend fun manualSearch(phoneNum: String, countryCode: String, countryIso: String): Response<SerachRes>?  = withContext(Dispatchers.IO){
        var result : Response<SerachRes>? = null
        try {
            Log.d(TAG, "manualSearch:hashednum $phoneNum")
//            val token = tokenManager.getDecryptedToken()
            val token =  tokenHelper?.getToken()
            if(!token.isNullOrEmpty()){
                result =  retrofitService?.search(SearchDTO(phoneNum), token)
                Log.d(TAG, "search: $result")
            }
        }catch (e:Exception){
            Log.d(TAG, "search:exception $e")
        }

        return@withContext result
    }

    suspend fun incrementTotalSpamCount()  = withContext(Dispatchers.IO) {
       try {
           val token:String? = tokenHelper?.getToken()


           token?.let { retrofitService!!.incrementTotalSpamCount(it) }
       }catch (e:Exception){
           Log.d(TAG, "incrementTotalSpamCount: ")
       }
    }

    suspend fun saveServerInfoIntoDB(contact: Contact) = withContext(Dispatchers.IO){
        var formatedNum = formatPhoneNumber(contact.phoneNumber)
        formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatedNum, countryIso )
        val res = callersInfoFromServerDAO?.find(formatedNum)

        if(res== null){
            val info = CallersInfoFromServer(
                contactAddress = formatedNum,
                spammerType = 0,
                firstName = contact?.firstName?:"",
                informationReceivedDate = Date(),
                spamReportCount = contact?.spamCount?:0L,
                city = contact?.location?:"",
                country = contact?.country?:"",
                carrier = contact?.carrier?:"",
                isBlockedByUser = false,
                isUserInfoFoundInServer = contact?.isInfoFoundInServer?:0,
                thumbnailImg = contact?.photoThumnailServer?:""
            )

            callersInfoFromServerDAO?.insert(listOf(info))
        }else{
            callersInfoFromServerDAO?.updateWithServerinfo(
                contactAddress = formatedNum,
                firstName = contact?.firstName?:"",
                lastName = contact?.lastName,
                informationReceivedDate = Date(),
                spamReportCount = contact?.spamCount?:0L,
                city = contact?.location?:"",
                country = contact?.country?:"",
                carrier = contact?.carrier?:"",
                thumbnailImg = contact?.photoThumnailServer?:""
                )
        }
    }

    /**
     * function to get contact details for a number
     */
    @SuppressLint("LongLogTag")
    suspend fun getContactDetailForNumberFromCp(phoneNumber: String, context:Context): Contact?  = withContext(
        Dispatchers.IO) {
        var cursor: Cursor? = null
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
            Log.d(ServerSearchViewModel.TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }
        return@withContext contact
    }
    suspend fun findOneInDb(fno: String, userSelectedCountryIso: String): CallersInfoFromServer?  = withContext(Dispatchers.IO){


        var formatedNum = formatPhoneNumber(fno)

        formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatedNum,
            this@SearchNetworkRepository.countryIso
        )
        return@withContext callersInfoFromServerDAO?.find(formatedNum)
    }

    suspend fun makeDelay() = withContext(Dispatchers.IO) {
        delay(100)
    }


    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}