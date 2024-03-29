package com.hashcaller.app.repository.search

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.hashcaller.app.Secrets
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.search.ISearchService
import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.search.ServerSearchViewModel
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
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
        try {
            var formatedNum = formatPhoneNumber(contact.phoneNumber)
            formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatedNum, countryIso )
            val hashedNum = Secrets().managecipher(null, formatedNum)
            val res = callersInfoFromServerDAO?.find(formatedNum)

            if(res== null){
                val info = CallersInfoFromServer(
                    contactAddress = formatedNum,
                    hashedNum = hashedNum,
                    spammerType = 0,
                    firstName = contact?.firstName?:"",
                    lastName = contact?.lastName?:"",
                    nameInPhoneBook = contact?.nameInPhoneBook?:"",
                    bio = contact?.bio?:"",
                    email = contact?.email?:"",
                    hUid = contact?.hUid?:"",
                    avatarGoogle = contact?.avatarGoogle?:"",
                    isVerifiedUser = contact?.isVerifiedUser?:false,
                    informationReceivedDate = Date(),
                    spamReportCount = contact?.spamCount?:0L,
                    city = contact?.location?:"",
                    country = contact?.country?:"",
                    carrier = contact?.carrier?:"",
                    isBlockedByUser = false,
                    isUserInfoFoundInServer = contact?.isInfoFoundInServer?:0,
                    thumbnailImg = contact?.photoThumnailServer?:""
                )
                Log.d(TAG, "saveServerInfoIntoDB: inserting $info")
                callersInfoFromServerDAO?.insert(listOf(info))
            }else{
                callersInfoFromServerDAO?.updateWithServerinfo(
                    contactAddress = formatedNum,
                    firstName = contact?.firstName?:"",
                    lastName = contact?.lastName,
                    nameInPhoneBook = contact?.nameInPhoneBook?:"",
                    informationReceivedDate = Date(),
                    spamReportCount = contact?.spamCount?:0L,
                    city = contact?.location?:"",
                    country = contact?.country?:"",
                    carrier = contact?.carrier?:"",
                    isUserInfoFoundInServer = contact?.isInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER,
                    spammerType = contact?.spamerType?:0 ,
                    thumbnailImg = contact?.photoThumnailServer?:"",
                    hUid = contact?.hUid?:"",
                    bio = contact?.bio?:"",
                    email = contact?.email?:"",
                    avatarGoogle = contact?.avatarGoogle?:"",
                    isVerifiedUser = contact?.isVerifiedUser?:false
                )
            }
        }catch (e:Exception){
            Log.d(TAG, "saveServerInfoIntoDB: $e")
        }
    }

    /**
     * function to get contact details for a number
     */
    @SuppressLint("LongLogTag")
    suspend fun getContactDetailForNumberFromCp(phoneNumber: String, context:Context): Contact?  = withContext(
        Dispatchers.IO) {
        var cursor: Cursor? = null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )

        var  contact:Contact? = null
        try{
            if(cursor2!=null && cursor2.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
                val name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
                val contactId = cursor2.getLong(cursor2.getColumnIndex("contact_id"))
                val normalizedNumber = cursor2.getString(cursor2.getColumnIndex("normalized_number"))
                contact = Contact(id=contactId, nameInPhoneBook= name,phoneNumber= normalizedNumber, firstName = "", photoThumnailServer = "")
            }


        }catch (e:Exception){
            Log.d(ServerSearchViewModel.TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }
        return@withContext contact
    }
    suspend fun findOneInDb(fno: String, userSelectedCountryIso: String): CallersInfoFromServer?  = withContext(Dispatchers.IO){
        return@withContext callersInfoFromServerDAO?.find(fno)
    }

    suspend fun makeDelay() = withContext(Dispatchers.IO) {
        delay(100)
    }


    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}