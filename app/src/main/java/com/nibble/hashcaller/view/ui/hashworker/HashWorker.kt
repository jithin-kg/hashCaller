package com.nibble.hashcaller.view.ui.hashworker

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
import com.nibble.hashcaller.view.ui.contacts.utils.hashUsingArgon
import com.nibble.hashcaller.work.ContactAddressWithHashDTO
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Class to perform argon2 hashing and save hash
 * and save it in hashedNumbers table
 * and insert hashed number to HashedNumber table
 *
 */
class HashWorker (private val context: Context,
                 private val params: WorkerParameters) : CoroutineWorker(context, params) {
    private val contactsCursor = context!!.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )
//    private var contactRepository: WorkerContactRepository = WorkerContactRepository(cursor)

    private val projection = arrayOf(CallLog.Calls.NUMBER)
    private val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    private val callLogCursor: Cursor? = context.contentResolver.query(
    CallLogLiveData.URI,
    projection,
    null,
    null,
    "${CallLog.Calls._ID} DESC"
    )
    private val hashedNumDao = HashCallerDatabase.getDatabaseInstance(context).hashedNumDAO()
    private val callerInfoFromSErverDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private val networkRepository = NumberUploaderRepository(TokenHelper(FirebaseAuth.getInstance().currentUser))
    private val repository: HashRepository = HashRepository(callLogCursor,
        contactsCursor,
        hashedNumDao,
        callerInfoFromSErverDAO
        )

    override suspend fun doWork(): Result {
        try {

            getUnknownNumbers()
            return Result.success()
        }catch (e:Exception){
            return Result.failure()
        }
    }

    private suspend fun getUnknownNumbers()  = withContext(Dispatchers.IO){
        var uploadOperationDefered : MutableList<Deferred<Unit>> = mutableListOf()
       val time=  measureTimeMillis {
            val defCallers =  async { getListOfAllUnkownCallers() }
            val listOfUnknownCallers = defCallers.await()
            var listOfHashedNumbers :MutableList<HashedNumber> = mutableListOf()
            var callersListChunkOfSize12 = listOfUnknownCallers.chunked(12)

               for (sublist in callersListChunkOfSize12){
                   val listOfUploadDTO: MutableList<ContactAddressWithHashDTO> = mutableListOf()
                   val timeInner = measureTimeMillis {
                   for (item in sublist){
                       var hashed:String?=  Secrets().managecipher(context.packageName, item)
                       hashed =  hashUsingArgon(hashed)
                       hashed?.let {
                           val hashedNumber = HashedNumber(item, it )
                           listOfHashedNumbers.add(hashedNumber)
                       }
                   }
                       hashedNumDao?.insert(listOfHashedNumbers)
               }
           }
        }
        Log.d(TAG, "getUnknownNumbers: time outer took $time")



//       val defContacts =  async { getListOfAllContacts() }
//        async { getListOfUnknownSMSsenders() }
    }

    private suspend fun uploadToServer(listOfUploadDTO: MutableList<ContactAddressWithHashDTO>) {
        val result = networkRepository.uploadNumbersToGetInfo(hashednums(listOfUploadDTO))

        var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

        if(result?.code() in (500..599)){
//            return Result.retry()
            Log.d(TAG, "uploadToServer: server error")
        }

        if(result!=null){
            for(cntct in result?.body()?.contacts!!){

                val callerInfoTobeSavedInDatabase = CallersInfoFromServer(
                    contactAddress = formatPhoneNumber(cntct.phoneNumber),
                    spammerType = 0,
                    firstName = cntct.firstName?:"",
                    informationReceivedDate = Date(),
                    spamReportCount =  cntct.spamCount,
                    isUserInfoFoundInServer = cntct.isInfoFoundInDb?:0,
                    thumbnailImg = cntct.imageThumbnail?:""
                )

                callerslistToBeSavedInLocalDb.add(callerInfoTobeSavedInDatabase)
            }
        }
        callersInfoFromServerDAO.insert(callerslistToBeSavedInLocalDb)
    }

    private suspend fun getListOfUnknownSMSsenders() {

    }

//    private suspend fun getListOfAllContacts(): MutableList<ContactUploadDTO> {
//        return contactRepository.fetchContacts()
//    }

    private suspend fun getListOfAllUnkownCallers(): MutableList<String> {

        return repository?.getListOfUnkownCallers()
    }
    companion object{
        const val TAG ="__HashWorker"
    }

}