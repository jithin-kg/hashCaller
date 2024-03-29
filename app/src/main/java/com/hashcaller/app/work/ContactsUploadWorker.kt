package com.hashcaller.app.work

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.Secrets

import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.contactInformation.ContactLastSyncedDate
import com.hashcaller.app.local.db.contactInformation.IContactIformationDAO
import com.hashcaller.app.local.db.contactInformation.IContactLastSycnedDateDAO
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.repository.contacts.*
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.work.CallNumUploadWorker
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.ui.sms.individual.util.SEARCHING_FOR_INFO
import com.hashcaller.app.view.ui.sms.individual.util.toast
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.util.*
import kotlin.jvm.Throws

/**
 * Created by Jithin KG on 25,July,2020
 * Todo update worker https://www.youtube.com/watch?v=6manrgTPzyA
 */
class ContactsUploadWorker(private val context: Context,private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<ContactUploadDTO>()
    private  var contactsListOf12: List<List<ContactUploadDTO>> = mutableListOf()
    private  var contactsListOf1000: List<List<ContactUploadDTO>> = mutableListOf()
//    context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO()
    private val contactLisDAO:IContactIformationDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    private val contactsLastSyncedDateDAO:IContactLastSycnedDateDAO = HashCallerDatabase.getDatabaseInstance(context).contactLastSyncedDateDAO()
    private val contactLocalSyncRepository = ContactLocalSyncRepository(contactLisDAO, context)
    private var contactRepository:WorkerContactRepository? = null
    private val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private val countryCodeIso = CountrycodeHelper(context).getCountryISO()
    val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    val hashedContactsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).hashedContactsDAO() }
    private val listToBeInsertedToDBFirst : MutableList<CallersInfoFromServer> = mutableListOf()
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)


    override suspend fun doWork(): Result  = withContext(Dispatchers.IO){
        try {
            val cursor = context!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
             contactRepository = WorkerContactRepository(
                 cursor,
                 countryCodeIso,
                 libCountryHelper
                 )
            val lastDate = contactsLastSyncedDateDAO.getLastSyncedDate()

            setNewlySavedContactsList()

            //first insert the list in DB,
                if(listToBeInsertedToDBFirst.isNotEmpty()){
                    callersInfoFromServerDAO.insert(listToBeInsertedToDBFirst)
                }

            val def1= async { sendContactsOfSize12() }
            val def2 = async { sendContactOfSize1000() }
            def1.await()
            def2.await()
        }catch (e:Exception){
            Log.d(TAG, "doWork: exception $e")
            return@withContext Result.retry()
        }
        return@withContext Result.success()
    }

    private suspend fun sendContactOfSize1000(): Int {
        if(!contactsListOf1000.isNullOrEmpty()){
            for (contactSublist in contactsListOf1000){
//                    val countryCode =   "91" //for emulator country code should be 91
                var countryISO = countryCodeIso
//                        val countryISO = "IN" //for testing in emulator coutry iso should be india otherwise it always returns us
                var countryCode = countryCodeIso

                val contactSyncDto = ContactsSaveDTO(contactSublist)
                val contactsNetworkRepository = ContactsNetworkRepository(context, tokenHelper)

                val result = contactsNetworkRepository.uploadContactsOf1000(contactSyncDto)
                Log.d(TAG, "sendContactOfSize1000: $result")
                var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()
                if(result?.code() in (500..599)){
                    return ERROR_WHILE_UPLODING
//                    throw  IOException()
//                    return Result.retry()
                }

//                if(result!=null){
//                    hashedContactsDAO.insert()
//                    val contactsList:MutableList<MyContacts> = mutableListOf()
//                    for(cntct in result?.body()?.contacts!!){
//                       contactsList.add(MyContacts(cntct.))
//                    }
//                }
////                    callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)
//                saveDateInContactLastSycnDate()
            }
        }
        return SUCCESS_UPLODING
    }

    @Throws(IOException::class)
    private suspend fun sendContactsOfSize12(): Int {
       try{
           if(!contactsListOf12.isNullOrEmpty()){
               for (contactSublist in contactsListOf12){
//                    val countryCode =   "91" //for emulator country code should be 91
                   var countryISO = countryCodeIso
//                        val countryISO = "IN" //for testing in emulator coutry iso should be india otherwise it always returns us
                   var countryCode = countryCodeIso
                   val contactSyncDto = ContactsSyncDTO(contactSublist, countryCode.toString(), countryISO)
                   val contactsNetworkRepository = ContactsNetworkRepository(context, tokenHelper)
                   val result = contactsNetworkRepository.uploadContacts(contactSyncDto)
                   Log.d(TAG, "sendContactsOfSize12: result $result")
                   var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()
                   if(result?.code() in (500..599)){
                       Log.d(TAG, "sendContactsOfSize12: server error")
                       return ERROR_WHILE_UPLODING
//                    return Result.retry()
                   }else if(result?.code() ==  429){
                       context.toast("Too many requests")
                   }

                   result?.let { rslt->
                       if(rslt.code() == HttpStatusCodes.STATUS_OK){
                           rslt?.body()?.let { ctcts->
                               for(cntct in ctcts.contacts){
                                   //todo do updation
                                   callersInfoFromServerDAO?.updateByHash(
                                       hashedNum = cntct.hash?:"",
                                       spamCount = cntct.spamCount?:0L,
                                       firstName = cntct.firstName?:"",
                                       lastName = cntct.lastName?:"",
                                       nameInPhoneBook = cntct.nameInPhoneBook?:"",
                                       date = Date(),
                                       isUserInfoFoundInServer = cntct.isInfoFoundInDb?: INFO_NOT_FOUND_IN_SERVER,
                                       thumbnailImg = cntct.imageThumbnail?:"",
                                       city = cntct.location?:"",
                                       carrier = cntct.carrier?:"",
                                       hUid = cntct.hUid?:"",
                                       bio = cntct.bio?:"",
                                       email = cntct.email?:"",
                                       avatarGoogle = cntct.avatarGoogle?:"",
                                       isVerifiedUser = cntct.isVerifiedUser?:false
                                       )
//                            callerslistToBeSavedInLocalDb.add(callerInfoTobeSavedInDatabase)
                               }
                           }

                       }
                   }

                   saveDateInContactLastSycnDate()
//                    callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)

               }
           }
       }catch (e:Exception){
           Log.d(TAG, "sendContactsOfSize12: $e")
       }
        return SUCCESS_UPLODING
    }


    @SuppressLint("LongLogTag")
    private suspend fun setNewlySavedContactsList() {
    try {
        val newlyCreatedContacts = mutableListOf<PhoneNumWithHashedNumDTO>()
        /**
         * All numbers are formatted to  ES164 standard
         */
        val  allcontactsInContentProvider =  contactRepository?.fetchContacts()

        if (allcontactsInContentProvider != null) {
            for(contact in allcontactsInContentProvider){
                if(!contact.phoneNumber.isNullOrEmpty()){
//                    formattedPhoneNum = libCountryHelper.getES164Formatednumber(formattedPhoneNum, countryIso = countryCodeHelper.getCountryISO())
//                    val res = contactLocalSyncRepository.getContact(formattedPhoneNum)
                    val res = callersInfoFromServerDAO.find(contact.phoneNumber)
                    var isTobeSearchedInServer = false
                    if(res==null ){
                        isTobeSearchedInServer = true
                    }
                    if(res!=null){
                        if(res?.isUserInfoFoundInServer == SEARCHING_FOR_INFO ){
                            isTobeSearchedInServer = true
                        }

                        if(Constants.isDataOutdated(
                                res.informationReceivedDate,
                                CallNumUploadWorker.NUMBER_OF_DAYS
                            )
                        ){
                            isTobeSearchedInServer = true
                        }
                    }
                    if(isTobeSearchedInServer){
                        var hashedPhoneNum:String? = Secrets().managecipher(context.packageName, contact.phoneNumber)
                        hashedPhoneNum?.let { hashed ->

                            insertIntoListSetUploadingStatus(contact.phoneNumber, hashed,
                            )

                            val cntctDtoObj = ContactUploadDTO(contact.name, hashed)
                            contacts.add(cntctDtoObj)
                        }
                    }
                }
            }
        }
        contactsListOf12 = contacts.chunked(12)
        contactsListOf1000 = contacts.chunked(1000)


    }catch (e:Exception){
        Log.d(TAG, "setNewlySavedContactsList: $e")
    }
    }

    suspend fun insertIntoListSetUploadingStatus(phoneNumber: String, hashed: String) {
        val callerInfoTobeSavedInDatabase = CallersInfoFromServer(
            contactAddress = phoneNumber,
            hashedNum = hashed,
            isUserInfoFoundInServer = SEARCHING_FOR_INFO,
            informationReceivedDate = Date(),
            )
        listToBeInsertedToDBFirst.add(callerInfoTobeSavedInDatabase)
    }
    private suspend fun saveDateInContactLastSycnDate() {
        this.contactsLastSyncedDateDAO.delteAll()
        this.contactsLastSyncedDateDAO.insert(ContactLastSyncedDate(null, Date()))
    }


   

    companion object{
        private const val TAG = "__ContactsUploadWorker"
        private const val ERROR_WHILE_UPLODING = 5
        private const val SUCCESS_UPLODING = 2
    }

}