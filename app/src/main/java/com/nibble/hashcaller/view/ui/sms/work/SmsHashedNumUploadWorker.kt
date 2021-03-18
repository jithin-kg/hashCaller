package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.work.ContactAddressWithHashDTO
import com.nibble.hashcaller.work.formatPhoneNumber
import com.nibble.hashcaller.work.replaceSpecialChars
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

    private val sMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).smsSenderInfoFromServerDAO()
    private val mutedSendersDAO = HashCallerDatabase.getDatabaseInstance(context).mutedSendersDAO()
    private val blockedOrSpamSenders = HashCallerDatabase.getDatabaseInstance(context).blockedOrSpamSendersDAO()
    private val spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
    private val smssendersInfoDAO = HashCallerDatabase.getDatabaseInstance(context).smsSenderInfoFromServerDAO()

    private val repository: SMScontainerRepository = SMScontainerRepository(
        context,
        sMSSendersInfoFromServerDAO,
        mutedSendersDAO,
        blockedOrSpamSenders
    )
    private val smsTracker:NewSmsTrackerHelper = NewSmsTrackerHelper( repository, sMSSendersInfoFromServerDAO)
    private lateinit var senderListTobeSendToServer: MutableList<ContactAddressWithHashDTO>
    private lateinit var senderListChuckOfSize12: List<List<ContactAddressWithHashDTO>>



    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result {
        try {
//            Log.d(TAG, "doWork: ")

            val smsrepoLocalRepository = SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO
            ) // to get content provided sms
            val allsmsincontentProvider = smsrepoLocalRepository.fetchSmsForWorker()
            val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
            val smsContainerRepository = SMScontainerRepository(
                context,
                smssendersInfoDAO,
                mutedSendersDAO,
                blockedOrSpamSenders
            )


            setlistOfAllUnknownSenders(allsmsincontentProvider, smssendersInfoDAO )


            if(senderListChuckOfSize12.isNotEmpty()){
                for (senderInfoSublist in senderListChuckOfSize12){

                    val result = smsContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))

                    var smsSenderlistToBeSavedToLocalDb : MutableList<SMSSendersInfoFromServer> = mutableListOf()

                    for(cntct in result.body()!!.contacts){
                        val formatedNum = replaceSpecialChars(cntct.phoneNumber)
                        val smsSenderTobeSavedToDatabase = SMSSendersInfoFromServer(
                            formatedNum, 0, cntct.name,
                            Date(), cntct.spamCount)
                        smsSenderlistToBeSavedToLocalDb.add(smsSenderTobeSavedToDatabase)
                    }
                    smssendersInfoDAO.insert(smsSenderlistToBeSavedToLocalDb)
                }
            }else{
                Log.d(TAG, "doWork: size less than 1")
            }



        }catch (e: HttpException){
            return Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return Result.success()
    }

    /**
     * sms senders address list in content provider - sms senders address list in localDB
     */
    @SuppressLint("LongLogTag")
    private suspend fun setlistOfAllUnknownSenders(
        allsmsincontentProvider: MutableList<SMS>,
        smssendersInfoDAO: SMSSendersInfoFromServerDAO
    ) {


        senderListTobeSendToServer  = mutableListOf()

        for (sms in allsmsincontentProvider){
            Log.d(TAG, "doWork: threadID ${sms.threadID}")

            val smssenderInfoAvailableInLocalDb=  smssendersInfoDAO.find(sms.addressString!!)

            if(smssenderInfoAvailableInLocalDb == null){
                Log.d(TAG, "doWork: no data recieved from server")

                val contactAddressWithoutSpecialChars = formatPhoneNumber(sms.addressString!!)

                val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                senderListTobeSendToServer.add(ContactAddressWithHashDTO(sms.addressString!!, hashedAddress))

            }else{
                val today = Date()
                if(isCurrentDateAndPrevDateisGreaterThanLimit(smssenderInfoAvailableInLocalDb.informationReceivedDate, NUMBER_OF_DAYS)){
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