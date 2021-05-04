package com.nibble.hashcaller.view.ui.call.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.repository.CallLocalRepository
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
        CoroutineWorker(context, params){

    val contacts = mutableListOf<ContactUploadDTO>()
    private val callersListDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private lateinit var callersListTobeSendToServer: MutableList<ContactAddressWithHashDTO>
    private lateinit var callersListChunkOfSize12: List<List<ContactAddressWithHashDTO>>



    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO){
        try {
            val networklivedta = ConnectionLiveData(context)

            Log.d(TAG, "doWork: ")
            val callersLocalRepository =
                CallLocalRepository(
                    context
                )
            val allcallsincontentProvider = callersLocalRepository.getCallLog()
            val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
            val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
            val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }

            val callContainerRepository =
                CallContainerRepository(
                    context,
                    callersInfoFromServerDAO,
                    mutedCallersDAO,
                    callLogDAO,
                    DataStoreRepository(context)
                )


            setlistOfAllUnknownCallers(allcallsincontentProvider, callersInfoFromServerDAO )


            if(callersListChunkOfSize12.isNotEmpty()){
                for (senderInfoSublist in callersListChunkOfSize12){
                    /**
                     * send the list to server
                     */
                    /**
                     * send the list to server
                     */
                    val result = callContainerRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))

                    var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

                    if(result.code() in (500..599)){
                        return@withContext Result.retry()
                    }

                    for(cntct in result.body()!!.contacts){
                        val callerInfoTobeSavedInDatabase = CallersInfoFromServer(null,
                            formatPhoneNumber(cntct.phoneNumber), 0, cntct.name,
                            Date(), cntct.spamCount)
                        callerslistToBeSavedInLocalDb.add(callerInfoTobeSavedInDatabase)
                    }
                    callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)
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
    ) {

        callersListTobeSendToServer  = mutableListOf()

        for (caller in allcallsInContentProvider){


            val callersInfoAvailableInLocalDb=  callerssInfoFromServerDAO.find(formatPhoneNumber(caller.number))

            if(callersInfoAvailableInLocalDb == null){
                Log.d(TAG, "doWork: no data available in db for number ${caller.number}")

                val contactAddressWithoutSpecialChars = formatPhoneNumber(caller.number!!)
                val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                callersListTobeSendToServer.add(ContactAddressWithHashDTO(formatPhoneNumber(caller.number!!), hashedAddress))

            }else{
                val today = Date()
                if(isCurrentDateAndPrevDateisGreaterThanLimit(callersInfoAvailableInLocalDb.informationReceivedDate, NUMBER_OF_DAYS)){
                    //We need to check if new data information about the number is available server
                    //todo this is getting called all the time, need to check on this

//                    Log.d(TAG, "doWork: outdated data")
                    val contactAddressWithoutSpecialChars = formatPhoneNumber(caller.number!!)
                    val hashedAddress = Secrets().managecipher(context.packageName,contactAddressWithoutSpecialChars)
                    callersListTobeSendToServer.add(ContactAddressWithHashDTO(formatPhoneNumber(caller.number!!), hashedAddress))
                }
//                    if(sms.currentDate)
                //Todo compare dates
            }

        }

        //if the size of the list is greater than 12 then splice it into chunks of 12 and rest
        //to reduce load on server, we only sends 12 items at a time
        callersListChunkOfSize12 = callersListTobeSendToServer.chunked(12)
        Log.d(TAG, "setlistOfAllUnknownSenders : chucked list is $callersListChunkOfSize12")


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