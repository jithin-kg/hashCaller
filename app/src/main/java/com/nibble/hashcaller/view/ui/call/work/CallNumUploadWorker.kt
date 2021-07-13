package com.nibble.hashcaller.view.ui.call.work

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
import com.nibble.hashcaller.network.HttpStatusCodes
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.repository.contacts.PhoneNumWithHashedNumDTO
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.repository.CallLocalRepository
import com.nibble.hashcaller.view.ui.sms.individual.util.SEARCHING_FOR_INFO
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
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO){
        try {


            val networklivedta = ConnectionLiveData(context)

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
                    var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()
                    //99e6b20ac2e79d44dc9fe018c188b5ec6fb10ad78a844cb9d67a6ba5f14b30a0 918086176331
                    if(result?.code() in (500..599)){
                        return@withContext Result.retry()
                    }

                    result?.let{ reslt->
                        if(reslt.code() == HttpStatusCodes.STATUS_OK){
                            for(cntct in reslt.body()?.contacts!!){
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

                    }



//                    callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)
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
                if(isCurrentDateAndPrevDateisGreaterThanLimit(callersInfoAvailableInLocalDb.informationReceivedDate, NUMBER_OF_DAYS)){

                    var hashedAddress:String? = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
//                    hashedAddress = hashUsingArgon(hashedAddress)
                    hashedAddress?.let {hashed->
                        callersListTobeSendToServer.add(ContactAddressWithHashDTO( hashed))
                        insertIntoListSetUploadingStatus(
                            caller.number,
                            hashed,
                        )
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
        private const val TAG = "__CallNumUploadWorker"
        const val NUMBER_OF_DAYS = 1
    }

}