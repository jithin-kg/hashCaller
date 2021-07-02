package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.repository.contacts.PhoneNumWithHashedNumDTO
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.individual.util.SEARCHING_FOR_INFO
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import com.nibble.hashcaller.work.ContactAddressWithHashDTO
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Jithin KG on 25,July,2020
 * Todo update worker https://www.youtube.com/watch?v=6manrgTPzyA
 *
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

    val callContainerRepository =
        CallContainerRepository(
            context,
            callersInfoFromServerDAO,
            mutedCallersDAO,
            callLogDAO,
            DataStoreRepository(context.tokeDataStore),
            tokenHelper,
            smsThreadsDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        )

    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "doWork: ")


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

                    if(result!=null){
                        for(cntct in result?.body()?.contacts!!){
                            var formated = formatPhoneNumber(cntct.hash)

                            formated = libCountryHelper.getES164Formatednumber(formated,countryCodeIso )

                            callersInfoFromServerDAO?.updateByHash(
                                hashedNum = cntct.hash,
                                spamCount = cntct.spamCount,
                                firstName = cntct.firstName,
                                lastName = "",
                                date = Date(),
                                isUserInfoFoundInServer = cntct.isInfoFoundInDb,
                                thumbnailImg = cntct.imageThumbnail?:"",
                                city = cntct.location,
                                carrier = cntct.carrier
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