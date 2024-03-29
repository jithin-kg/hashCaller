package com.hashcaller.app.view.ui.call.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.Secrets
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.Constants.Companion.isDataOutdated
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.dialer.util.CallLogData
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository
import com.hashcaller.app.view.ui.call.repository.CallLocalRepository
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.ui.sms.individual.util.SEARCHING_FOR_INFO
import com.hashcaller.app.view.ui.sms.individual.util.toast
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.ContactAddressWithHashDTO
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

/**
 * Created by Jithin KG on 25,July,2020
 *
 * worker for uploading callers  number to server inorder to  get info about the callers
 */

class CallNumUploadWorker(private val context: Context, private val params:WorkerParameters ) :
        CoroutineWorker(context, params) {

    val contacts = mutableListOf<PhoneNumWithHashedNumDTO>()
    private val callersListDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private lateinit var callersListTobeSendToServer: MutableList<ContactAddressWithHashDTO>
    private lateinit var callersListChunkOfSize12: List<List<ContactAddressWithHashDTO>>
    val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
    val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }
    val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }
    private val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private val countryCodeIso = CountrycodeHelper(context).getCountryISO()
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)
    private val listToBeInsertedToDBFirst : MutableList<CallersInfoFromServer> = mutableListOf()
    private var spamThreshold = Constants.DEFAULT_SPAM_THRESHOLD
    private val dataStoreRepository  = DataStoreRepository(context.tokeDataStore)

    private lateinit var  callContainerRepository:CallContainerRepository



    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO){
        try {

            spamThreshold = dataStoreRepository.getInt(SPAM_THRESHOLD)?: DEFAULT_SPAM_THRESHOLD
            callContainerRepository =  CallContainerRepository(
                context,
                callersInfoFromServerDAO,
                mutedCallersDAO,
                callLogDAO,
                DataStoreRepository(context.tokeDataStore),
                tokenHelper,
                smsThreadsDAO,
                LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
                CountrycodeHelper(context).getCountryISO(),
                spamThreshold
            )

            val callersLocalRepository =
                CallLocalRepository(
                    context,
                    countryCodeIso,
                    libCountryHelper
                )

            /**
             * The returned numbers are formated to ES16 standard
             */
            val allcallsincontentProvider = callersLocalRepository.getCallLog()

            setlistOfAllUnknownCallers(allcallsincontentProvider, callersInfoFromServerDAO )

            //first insert the list in DB
            if(listToBeInsertedToDBFirst.isNotEmpty()){
                callersInfoFromServerDAO.insert(listToBeInsertedToDBFirst)
            }
            if(callersListChunkOfSize12.isNotEmpty()){
                for (senderInfoSublist in callersListChunkOfSize12){
                    /**
                     * send the list to server
                     */
                    val result = callContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))
                    Log.d(TAG, "doWork: $result")
                    var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()
                    //99e6b20ac2e79d44dc9fe018c188b5ec6fb10ad78a844cb9d67a6ba5f14b30a0 918086176331
                    if(result?.code() in (500..599)){
                        return@withContext Result.retry()
                    }else if(result?.code() ==  429){
                        context.toast("Too many requests")
                    }
                    
                    result?.let{ reslt->
                        if(reslt.code() == HttpStatusCodes.STATUS_OK){
                            for(cntct in reslt.body()?.contacts!!){
//                                var formated = formatPhoneNumber(cntct.hash)
                                if(!cntct.hUid.isNullOrEmpty()){
                                    Log.d(TAG+"huid", "doWork: ${cntct.hUid}")
                                }
                                if(cntct.firstName == "Sathiamma"){
                                    Log.d(TAG+"name", "doWork:Sathiamma")
                                }
//                                formated = libCountryHelper.getES164Formatednumber(formated,countryCodeIso )
//                                if(!cntct.avatarGoogle.isNullOrEmpty()){
//                                    Log.d(TAG, "doWork: avatarGoogle not empty")
//                                }
                                callersInfoFromServerDAO?.updateByHash(
                                    hashedNum = cntct.hash?:"",
                                    spamCount = cntct.spamCount?:0L,
                                    firstName = cntct.firstName?:"",
                                    lastName = cntct.lastName?:"",
                                    nameInPhoneBook =cntct.nameInPhoneBook?:"",
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

//                           callerslistToBeSavedInLocalDb.add(callerInfoTobeSavedInDatabase)
                            }
                        }

                    }



//                    callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)
                }
            }else{
                Log.d(TAG, "doWork: size less than 1")
            }
                


        }catch (e: HttpException) {
            return@withContext Result.retry()
            Log.d(TAG, "doWork: retry")
        }

        return@withContext Result.success()
    }

    /**
     * callers address list in content provider - (minus) sms senders address list in localDB
     */
    @SuppressLint("LongLogTag")
    private suspend fun setlistOfAllUnknownCallers(
        allcallsInContentProvider: List<CallLogData>,
        callerssInfoFromServerDAO: CallersInfoFromServerDAO
    )  = withContext(Dispatchers.IO){

        callersListTobeSendToServer  = mutableListOf()

        for (caller in allcallsInContentProvider){

            val contactAddressWithoutSpecialChars = libCountryHelper.getES164Formatednumber(formatPhoneNumber(caller.number), countryCodeIso)

            val callersInfoAvailableInLocalDb=  callerssInfoFromServerDAO.find(contactAddressWithoutSpecialChars)

            if(callersInfoAvailableInLocalDb == null){

                var hashedAddress:String? = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
//                hashedAddress = hashUsingArgon(hashedAddress)
                hashedAddress?.let {hashed->
                    callersListTobeSendToServer.add(ContactAddressWithHashDTO(hashed))
                    insertIntoListSetUploadingStatus(
                        caller.number,
                        hashed,
                    )
                }

            }else{
                if(isDataOutdated(callersInfoAvailableInLocalDb.informationReceivedDate, NUMBER_OF_DAYS)){

                    var hashedAddress:String? = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
//                    hashedAddress = hashUsingArgon(hashedAddress)
                    hashedAddress?.let {hashed->
                        callersListTobeSendToServer.add(ContactAddressWithHashDTO( hashed))
//                        insertIntoListSetUploadingStatus(
//                            caller.number,
//                            hashed,
//                        )
                    }
                }
//                    if(sms.currentDate)
                //Todo compare dates
            }

        }

        //if the size of the list is greater than 12 then splice it into chunks of 12 and rest
        //to reduce load on server, we only sends 12 items at a time
        callersListChunkOfSize12 = callersListTobeSendToServer.chunked(12)


    }

    suspend fun insertIntoListSetUploadingStatus(phoneNumber: String, hashed: String) {
        val callerInfoTobeSavedInDatabase = CallersInfoFromServer(
            contactAddress = phoneNumber,
            hashedNum = hashed,
            isUserInfoFoundInServer = SEARCHING_FOR_INFO,
            informationReceivedDate = Date(2323223232L),
        )
        listToBeInsertedToDBFirst.add(callerInfoTobeSavedInDatabase)

    }



//    private suspend fun uploadnumbersToServer(allsmswithoutspam: MutableList<SMS>): Response<UnknownSMSsendersInfoResponse> {
//        val unkownsmsnumberslist = smsTracker.getUnknownNumbersList(allsmswithoutspam, context.packageName)
//        if(!unkownsmsnumberslist.isNullOrEmpty())
//         return repository.uploadNumbersToGetInfo(hashednums(unkownsmsnumberslist))
//
//        return
//    }



    companion object  {
        private const val TAG = "__CallNumUploadWorker"
        const val NUMBER_OF_DAYS = 7
    }

}