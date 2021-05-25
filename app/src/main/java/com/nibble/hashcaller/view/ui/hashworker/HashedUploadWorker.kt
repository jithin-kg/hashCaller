package com.nibble.hashcaller.view.ui.hashworker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.contacts.utils.DATE_THREASHOLD
import com.nibble.hashcaller.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import com.nibble.hashcaller.work.ContactAddressWithHashDTO
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

/**
 * Worker class that upload
 * the hashed number exist in HashedNumber table which have no info from server or
 * outdated information
 * and saves the response in database
 */
class HashedUploadWorker(private val context: Context,
                         private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val hashedNumDao = HashCallerDatabase.getDatabaseInstance(context).hashedNumDAO()
    private val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    private  var callersListTobeSendToServer: MutableList<ContactAddressWithHashDTO> = mutableListOf()
    private  var callersListChunkOfSize12:  List<List<ContactAddressWithHashDTO>> = mutableListOf()
    private val networkRepository = NumberUploaderRepository(TokenHelper(FirebaseAuth.getInstance().currentUser))
    private val contactLisDAO: IContactIformationDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    private val hashedContactsDAO = HashCallerDatabase.getDatabaseInstance(context).hashedContactsDAO()

    override suspend fun doWork(): Result {
        try {
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scope.launch {
                val allHashedItems = hashedNumDao.getAll()
                for (item in allHashedItems){
                    var isToBeSearchedInServer = false
                    val res = callersInfoFromServerDAO?.find(item.number)
                    if(res!=null){
                        if(isCurrentDateAndPrevDateisGreaterThanLimit(res.informationReceivedDate, DATE_THREASHOLD)){
                            isToBeSearchedInServer = true
                        }
                    }else{
                        isToBeSearchedInServer = true
                    }
                    if(isToBeSearchedInServer){
                        callersListTobeSendToServer.add(ContactAddressWithHashDTO(item.number, item.hashedNumber))
                    }
                }
                callersListChunkOfSize12 = callersListTobeSendToServer.chunked(12)

                //upload the item by 12
                if(callersListChunkOfSize12.isNotEmpty()){
                    for (senderInfoSublist in callersListChunkOfSize12){
                        /**
                         * send the list to server
                         */

                        val result = networkRepository.uploadNumbersToGetInfo(hashednums(senderInfoSublist))

                        var callerslistToBeSavedInLocalDb : MutableList<CallersInfoFromServer> = mutableListOf()

                        if(result?.code() in (500..599)){
//                            return@launch Result.retry()

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
                }else{
                    Log.d(TAG, "doWork: size less than 1")
                }
            }.join()

            return Result.success()
        }catch (e:Exception){
            Log.d(TAG, "doWork: $e")
            return Result.failure()
        }
    }
    companion object{
        const val TAG = "__HashedUploadWorker"
    }
}