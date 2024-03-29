package com.hashcaller.app.view.ui.sms.work

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
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository
import com.hashcaller.app.view.ui.sms.SMScontainerRepository
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.ui.sms.individual.util.SEARCHING_FOR_INFO
import com.hashcaller.app.view.ui.sms.util.SMS
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.ContactAddressWithHashDTO
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Jithin KG on 25,July,2020
 * worker for uploading sms senders number to server to get info about the senders
 */
class SmsHashedNumUploadWorker(private val context: Context, private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<PhoneNumWithHashedNumDTO>()

    private val sMSSendersInfoFromServerDAO: CallersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private val mutedSendersDAO = HashCallerDatabase.getDatabaseInstance(context).mutedSendersDAO()
    private val blockedOrSpamSenders = HashCallerDatabase.getDatabaseInstance(context).blockedOrSpamSendersDAO()
    private val spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
    private val smssendersInfoDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)
    private val countryCodeHelper = CountrycodeHelper(context)
    private val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private val smsRepository:SMSWorkerRepository = SMSWorkerRepository(context, libCountryHelper,countryCodeHelper )
    private val listToBeInsertedToDBFirst : MutableList<CallersInfoFromServer> = mutableListOf()


    private val repository: SMScontainerRepository = SMScontainerRepository(
        context,
        sMSSendersInfoFromServerDAO,
        mutedSendersDAO,
        blockedOrSpamSenders,
        DataStoreRepository(context.tokeDataStore),
        tokenHelper
    )
    private val countryCodeIso = CountrycodeHelper(context).getCountryISO()
    private val smsTracker:NewSmsTrackerHelper = NewSmsTrackerHelper( repository, sMSSendersInfoFromServerDAO)
    private lateinit var senderListTobeSendToServer: MutableList<ContactAddressWithHashDTO>
    private lateinit var senderListChuckOfSize12: List<List<ContactAddressWithHashDTO>>
    private val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }
    private val callLogDAO = context?.let{HashCallerDatabase.getDatabaseInstance(it).callLogDAO()}
    val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
    private var spamThreshold = Constants.DEFAULT_SPAM_THRESHOLD
    private val dataStoreRepository  = DataStoreRepository(context.tokeDataStore)
    private lateinit var  callContainerRepository:CallContainerRepository


    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "doWork: ")
            spamThreshold = dataStoreRepository.getInt(PreferencesKeys.SPAM_THRESHOLD)?: Constants.DEFAULT_SPAM_THRESHOLD
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

            val allsmsincontentProvider = smsRepository.fetchSmsForWorker()
            val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
            val smsContainerRepository = SMScontainerRepository(
                context,
                callerInfoFromServerDAO,
                mutedSendersDAO,
                blockedOrSpamSenders,
                DataStoreRepository(context.tokeDataStore),
                tokenHelper
            )


            setlistOfAllUnknownSenders(allsmsincontentProvider, callerInfoFromServerDAO )

            //first insert the list in DB
            if(listToBeInsertedToDBFirst.isNotEmpty()){
                callerInfoFromServerDAO.insert(listToBeInsertedToDBFirst)
            }
            if(senderListChuckOfSize12.isNotEmpty()){
                for (senderInfoSublist in senderListChuckOfSize12){


                    val result = callContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))
                    var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

                    if(result?.code() in (500..599)){
                        return@withContext Result.retry()
                    }

                    result?.body()?.let{ body->

                        for(cntct in body.contacts){
                            var formated = formatPhoneNumber(cntct.hash)

                            formated = libCountryHelper.getES164Formatednumber(formated,countryCodeIso )

                            callersInfoFromServerDAO?.updateByHash(
                                hashedNum = cntct.hash,
                                spamCount = cntct.spamCount?:0,
                                firstName = cntct.firstName?:"",
                                nameInPhoneBook = cntct.nameInPhoneBook?:"",
                                lastName = "",
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
//                    val result = smsContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))
//
//                    if(result?.code() in (500..599)){
//                        return@withContext Result.retry()
//                    }
//                    var smsSenderlistToBeSavedToLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

//                    if(result?.body() != null){
//                        for(cntct in result?.body()!!.contacts){
//                            val formatedNum = libCountryHelper.getES164Formatednumber(formatPhoneNumber(cntct.phoneNumber), countryCodeIso)
//
//                            //todo add carrier information and geolocation info for number
//                            var hashedAddress:String? = Secrets().managecipher(context.packageName,formatedNum)
////                            val smsSenderTobeSavedToDatabase = CallersInfoFromServer(
////                                formatedNum, hashedNum = hashedAddress!!,
////                                spammerType = 0,
////                                firstName = cntct.name,
////                               "",
////                                Date(),
////                                )
////                            callerInfoFromServerDAO?.updateByHash(
////                                hashedNum = cntct.hash,
////                                spamCount = cntct.spamCount,
////                                firstName = cntct.firstName,
////                                lastName = "cntct.lastName",
////                                date = Date(),
////                                isUserInfoFoundInServer = cntct.isInfoFoundInDb,
////                                thumbnailImg = cntct.imageThumbnail?:"",
////                                city = cntct.location,
////                                carrier = cntct.carrier
////
////                            )
////                            smsSenderlistToBeSavedToLocalDb.add(smsSenderTobeSavedToDatabase)
//                        }
//                    }

//                    callerInfoFromServerDAO.insert(smsSenderlistToBeSavedToLocalDb)
                }
            }else{
                Log.d(TAG, "doWork: size less than 1")
            }



        }catch (e: HttpException){
            return@withContext Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return@withContext Result.success()
    }

    /**
     * sms senders address list in content provider - sms senders address list in localDB
     */
    @SuppressLint("LongLogTag")
    private suspend fun setlistOfAllUnknownSenders(
        allsmsincontentProvider: MutableList<SMS>,
        callerInfoFromServerDAO: CallersInfoFromServerDAO
    ) {


        senderListTobeSendToServer  = mutableListOf()

        for (sms in allsmsincontentProvider){
            Log.d(TAG, "doWork: threadID ${sms.threadID}")
            var queryNum = ""

                queryNum = formatPhoneNumber(sms.addressString!!)

             callerInfoFromServerDAO.find(libCountryHelper.getES164Formatednumber(queryNum, countryCodeIso)).apply {
                 if(this == null){
                     Log.d(TAG, "doWork: no data recieved from server")

                     val contactAddressWithoutSpecialChars = formatPhoneNumber(sms.addressString!!)

                     var hashedAddress:String? = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
//
//                     hashedAddress = hashUsingArgon(hashedAddress)
                     hashedAddress?.let {hashed->
                         senderListTobeSendToServer.add(ContactAddressWithHashDTO( hashed))
                         insertIntoListSetUploadingStatus(
                             contactAddressWithoutSpecialChars,
                             hashed,
                         )
                     }

                 }else{
                     val today = Date()
                     if(isCurrentDateAndPrevDateisGreaterThanLimit(this.informationReceivedDate, NUMBER_OF_DAYS)) {
                         val contactAddressWithoutSpecialChars = formatPhoneNumber(sms.addressString!!)
                         val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                         senderListTobeSendToServer.add(
                             ContactAddressWithHashDTO
                                 (hashedAddress)
                         )
                         insertIntoListSetUploadingStatus(
                             contactAddressWithoutSpecialChars,
                             hashedAddress,
                         )
                     }
//                    if(sms.currentDate)
                     //Todo compare dates
                 }
            }



        }

        //if the size of the list is greater than 12 then splice it into chunks of 12 and rest
        //to reduce load on server, we only sends 12 items at a time
        senderListChuckOfSize12 = senderListTobeSendToServer.chunked(12)
        Log.d(TAG, "setlistOfAllUnknownSenders : chucked list is $senderListChuckOfSize12")


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

    /**
     * @param informationReceivedDate : date at which the data is inserted in db
     * @param limit : number of day in which a lookup for the current number should perform
     */

     fun isCurrentDateAndPrevDateisGreaterThanLimit(
        informationReceivedDate: Date,
        limit: Int
    ): Boolean {
        val today = Date()
        val miliSeconds: Long = today.getTime() - informationReceivedDate.getTime()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
        val minute = seconds / 60
        val hour = minute / 60
        val days = hour / 24
        if(days > limit)
            return true
        return false

    }

//    private suspend fun uploadnumbersToServer(allsmswithoutspam: MutableList<SMS>): Response<UnknownSMSsendersInfoResponse> {
//        val unkownsmsnumberslist = smsTracker.getUnknownNumbersList(allsmswithoutspam, context.packageName)
//        if(!unkownsmsnumberslist.isNullOrEmpty())
//         return repository.uploadNumbersToGetInfo(hashednums(unkownsmsnumberslist))
//
//        return
//    }



    companion object{
        private const val TAG = "__SmsHashedNumUploadWorker"
        const val NUMBER_OF_DAYS = 1
    }

}