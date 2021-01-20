package com.nibble.hashcaller.view.ui.sms.work

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
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Jithin KG on 25,July,2020
 * Todo update worker https://www.youtube.com/watch?v=6manrgTPzyA
 */
class SmsHashedNumUploadWorker(private val context: Context, private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<ContactUploadDTO>()
//    context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO()
    //todo the response may also contains non spammers, I need to create seperate table for spammers and non spammer

    private val SMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).spammerInfoFromServerDAO()
    private val repository: SMScontainerRepository = SMScontainerRepository(context, SMSSendersInfoFromServerDAO)
    private val smsTracker:NewSmsTrackerHelper = NewSmsTrackerHelper( repository, SMSSendersInfoFromServerDAO)
    private val spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()


    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "doWork: ")

            val smsrepoLocalRepository = SMSLocalRepository(context, spamListDAO) // to get content provided sms
            val allsmsincontentProvider = smsrepoLocalRepository.fetchSMS(null)
            val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spammerInfoFromServerDAO() }
            val smsContainerRepository = SMScontainerRepository(context, smssendersInfoDAO )
            val senderListTobeSendToServer: MutableList<String> = mutableListOf()

            for (sms in allsmsincontentProvider){
//                if(sms.addressString!!.length>4){
//                    val firstFiveDigitsOfNum = sms.addressString!!.substring(0, 4)
//                }

                 val encodedAndHashedPhoneNumber = Secrets().managecipher(context?.packageName!!, sms.addressString.toString()) // encoding the

               val smssenderInfoAvailableInLocalDb=  smssendersInfoDAO.find(encodedAndHashedPhoneNumber)
              
                if(smssenderInfoAvailableInLocalDb == null){
                    Log.d(TAG, "doWork: no data recieved from server")
                    senderListTobeSendToServer.add(encodedAndHashedPhoneNumber)

                }else{
                    val today = Date()
                    if(isCurrentDateAndPrevDateisGreaterThanLimit(smssenderInfoAvailableInLocalDb.informationReceivedDate, NUMBER_OF_DAYS)){
                        //We need to check if new data information about the number is available server
                        Log.d(TAG, "doWork: outdated data")
                        senderListTobeSendToServer.add(encodedAndHashedPhoneNumber)
                    }
//                    if(sms.currentDate)
                    //Todo compare dates
                }

            }
            
            if(senderListTobeSendToServer.size >0){
                
                val result = smsContainerRepository.uploadNumbersToGetInfo(hashednums(senderListTobeSendToServer))
                var smsSenderlistToBeSavedToLocalDb : MutableList<SMSSendersInfoFromServer> = mutableListOf()
                for(cntct in result.body()!!.contacts){
                    val smsSenderTobeSavedToDatabase = SMSSendersInfoFromServer(null,
                        cntct.oldHash, 0, cntct.name,
                              Date(), 10, "8080878")
                    smsSenderlistToBeSavedToLocalDb.add(smsSenderTobeSavedToDatabase)
                }
                smssendersInfoDAO.insert(smsSenderlistToBeSavedToLocalDb)
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
     * @param informationReceivedDate : date at which the data is inserted in db
     * @param limit : number of day in which a lookup for the current number should perform
     */
    private fun isCurrentDateAndPrevDateisGreaterThanLimit(
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