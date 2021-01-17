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
            val senderListTobeSendToServer: MutableList<String> = mutableListOf()

            for (sms in allsmsincontentProvider){
                val encodedAndHashedPhoneNumber = Secrets().managecipher(context?.packageName!!, sms.addressString.toString()) // encoding the

               val smssenderInfo=  smssendersInfoDAO.get(encodedAndHashedPhoneNumber)
                if(smssenderInfo == null){

                    senderListTobeSendToServer.add(encodedAndHashedPhoneNumber)
                    //save the details once and update when the result comes from the server
//                    val rowSmssenderInfo = SMSSendersInfoFromServer(null,
//                        encodedAndHashedPhoneNumber, -1,
//                        sms.addressString.toString(), )
                }
            }


//            val unkownsmsnumberslist = smsTracker.getUnknownNumbersList(allsmswithoutspam, context.packageName)
//            if(!unkownsmsnumberslist.isNullOrEmpty() ){
//                if(unkownsmsnumberslist.isNotEmpty()){
//                    val response =  repository.uploadNumbersToGetInfo(hashednums(unkownsmsnumberslist))
//                    Log.d(TAG, "doWork: response is $response")
//                    Log.d(TAG, "doWork: response body ${response.body()}")
//                }
//
//            }else{
//                Log.d(TAG, "doWork: unkownsmsnumberslist is empty")
//            }

        }catch (e: HttpException){
            return Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return Result.success()
    }

//    private suspend fun uploadnumbersToServer(allsmswithoutspam: MutableList<SMS>): Response<UnknownSMSsendersInfoResponse> {
//        val unkownsmsnumberslist = smsTracker.getUnknownNumbersList(allsmswithoutspam, context.packageName)
//        if(!unkownsmsnumberslist.isNullOrEmpty())
//         return repository.uploadNumbersToGetInfo(hashednums(unkownsmsnumberslist))
//
//        return
//    }



    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}