package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
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
    val contacts = mutableListOf<ContactUploadDTO>()

    private val sMSSendersInfoFromServerDAO: CallersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private val mutedSendersDAO = HashCallerDatabase.getDatabaseInstance(context).mutedSendersDAO()
    private val blockedOrSpamSenders = HashCallerDatabase.getDatabaseInstance(context).blockedOrSpamSendersDAO()
    private val spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
    private val smssendersInfoDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)

    private val repository: SMScontainerRepository = SMScontainerRepository(
        context,
        sMSSendersInfoFromServerDAO,
        mutedSendersDAO,
        blockedOrSpamSenders,
        DataStoreRepository(context.tokeDataStore),
        tokenHelper
    )
    private val smsTracker:NewSmsTrackerHelper = NewSmsTrackerHelper( repository, sMSSendersInfoFromServerDAO)
    private lateinit var senderListTobeSendToServer: MutableList<ContactAddressWithHashDTO>
    private lateinit var senderListChuckOfSize12: List<List<ContactAddressWithHashDTO>>
    private val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }



    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "doWork: ")

            val smsrepoLocalRepository = SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO,
                DataStoreRepository(context.tokeDataStore),
                TokenHelper( FirebaseAuth.getInstance().currentUser)
            ) // to get content provided sms

            val allsmsincontentProvider = smsrepoLocalRepository.fetchSmsForWorker()
            val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
            val smsContainerRepository = SMScontainerRepository(
                context,
                smssendersInfoDAO,
                mutedSendersDAO,
                blockedOrSpamSenders,
                DataStoreRepository(context.tokeDataStore),
                tokenHelper
            )


            setlistOfAllUnknownSenders(allsmsincontentProvider, smssendersInfoDAO )


            if(senderListChuckOfSize12.isNotEmpty()){
                for (senderInfoSublist in senderListChuckOfSize12){

                    val result = smsContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))

                    if(result?.code() in (500..599)){
                        return@withContext Result.retry()
                    }
                    var smsSenderlistToBeSavedToLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

                    if(result?.body() != null){
                        for(cntct in result?.body()!!.contacts){
                            val formatedNum = formatPhoneNumber(cntct.phoneNumber)
                            val smsSenderTobeSavedToDatabase = CallersInfoFromServer(
                                formatedNum, 0, cntct.name,
                               "", Date())
                            smsSenderlistToBeSavedToLocalDb.add(smsSenderTobeSavedToDatabase)
                        }
                    }

                    smssendersInfoDAO.insert(smsSenderlistToBeSavedToLocalDb)
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
        smssendersInfoDAO: CallersInfoFromServerDAO
    ) {


        senderListTobeSendToServer  = mutableListOf()

        for (sms in allsmsincontentProvider){
            Log.d(TAG, "doWork: threadID ${sms.threadID}")
            var queryNum = ""

                queryNum = formatPhoneNumber(sms.addressString!!)

             smssendersInfoDAO.find(queryNum).apply {
                 if(this == null){
                     Log.d(TAG, "doWork: no data recieved from server")

                     val contactAddressWithoutSpecialChars = formatPhoneNumber(sms.addressString!!)

                     val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                     senderListTobeSendToServer.add(ContactAddressWithHashDTO(contactAddressWithoutSpecialChars, hashedAddress))

                 }else{
                     val today = Date()
                     if(isCurrentDateAndPrevDateisGreaterThanLimit(this.informationReceivedDate, NUMBER_OF_DAYS)){
                         //We need to check if new data information about the number is available server
                         //todo uncomment this and run, because this is called always and need to check on that
//                    Log.d(TAG, "doWork: outdated data")
                         val contactAddressWithoutSpecialChars = formatPhoneNumber(sms.addressString!!)
                         val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                         senderListTobeSendToServer.add(ContactAddressWithHashDTO(sms.addressString!!, hashedAddress))
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