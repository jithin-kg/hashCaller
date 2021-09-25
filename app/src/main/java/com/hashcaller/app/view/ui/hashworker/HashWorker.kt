package com.hashcaller.app.view.ui.hashworker

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.Secrets
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.dialer.util.CallLogLiveData
import com.hashcaller.app.view.ui.sms.util.SMSContract
import com.hashcaller.app.work.ContactAddressWithHashDTO
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashSet
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


    private val callersInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    private var callLogCursor: Cursor?= null
    private var smsCurosor:Cursor? = null
    private val hashedNumDao = HashCallerDatabase.getDatabaseInstance(context).hashedNumDAO()
    private val hashedContactsDAO = HashCallerDatabase.getDatabaseInstance(context).hashedContactsDAO()
    private val callerInfoFromSErverDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    private val networkRepository = NumberUploaderRepository(TokenHelper(FirebaseAuth.getInstance().currentUser))
    private var setOfContacts: HashSet<String> = hashSetOf()
    private var setofHashedAddressInDb:HashSet<String> = hashSetOf()


    private lateinit var repository: HashRepository

    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "doWork: starting work")
                initRepository()
                //insert the unknown callers number into db(number, argonhashedNumber)
                val listOfUnknownCallers =  repository?.getListOfUnkownCallers()
                insertHashedNumsIntoDatabase(listOfUnknownCallers)

                //insert unknonw sms sender into db (number, argonahsednumber)
                val listOfUnknownSMSSEnders =  repository?.getListOfAllUnkonSMSSenders()
                insertHashedNumsIntoDatabase(listOfUnknownSMSSEnders)

                //todo save the contacts to hashedContactsTable and observe it on call logs,
                //and upload to server and save in server and get info for that
////                //insert contacts into db (number, argonhashedNumber)
                val listOfUnknownContacts = setOfContacts.toList()

                Log.d(TAG, "getUnknownNumbers: all opearations completed")


            return Result.success()
        }catch (e:Exception){
            Log.d(TAG, "doWork: $e")
            return Result.failure()
        }
    }

    private suspend fun initRepository() = withContext(Dispatchers.IO) {
        setOfContacts = getSetOfConatcts()

        createCursors()
        val hashedItemsInDb = hashedNumDao.getAll()
        hashedItemsInDb.forEach {
           setofHashedAddressInDb.add(it.number)
        }
        repository=  HashRepository(callLogCursor,
            contactsCursor,
            hashedNumDao,
            callerInfoFromSErverDAO,
            setOfContacts,
            smsCurosor,
            setofHashedAddressInDb
        )
    }

    private fun createCursors() {
        val projectionCallLog = arrayOf(CallLog.Calls.NUMBER)
        callLogCursor = context.contentResolver.query(
            CallLogLiveData.URI,
            projectionCallLog,
            null,
            null,
            "${CallLog.Calls._ID} DESC"
        )
        smsCurosor = context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            null,
            null,
           null,
            "_id DESC"
        )


    }



    private suspend fun insertHashedNumsIntoDatabase(listOfUnknownCallers: List<String>) {
        var listOfHashedNumbers :MutableList<HashedNumber> = mutableListOf()
        var callersListChunkOfSize12 = listOfUnknownCallers.chunked(12)

        for (sublist in callersListChunkOfSize12){
            val listOfUploadDTO: MutableList<ContactAddressWithHashDTO> = mutableListOf()
            val timeInner = measureTimeMillis {
                for (item in sublist){
                    val formtedNumber = formatPhoneNumber(item)
                    var hashed:String?=  Secrets().managecipher(context.packageName, formtedNumber)
//                    hashed =  hashUsingArgon(hashed)
                    hashed?.let {
                        val hashedNumber = HashedNumber(formtedNumber, it )
                        listOfHashedNumbers.add(hashedNumber)
                    }
                }
                hashedNumDao?.insert(listOfHashedNumbers)
            }
        }
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
                    contactAddress = formatPhoneNumber(cntct.hash),
                    spammerType = 0,
                    firstName = cntct.firstName?:"",
                    informationReceivedDate = Date(),
                    spamReportCount =  cntct.spamCount?:0,
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

    private suspend fun getSetOfConatcts(): HashSet<String> {
        var hashSetOfAddress : HashSet<String> = HashSet()
        try {
            if (contactsCursor?.count ?: 0 > 0) {
                while (contactsCursor!!.moveToNext()) {
                    var contact = PhoneNumWithHashedNumDTO()
                    val name =
                        contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var phoneNo =
                        contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNo = formatPhoneNumber(phoneNo)
                    if(!hashSetOfAddress.contains(phoneNo)){
                        hashSetOfAddress.add(phoneNo)
                    }else{
                        continue
                    }
                }
                contactsCursor.close()
            }
        }catch (e:Exception){
            Log.d(HashRepository.TAG, "getListOfConatcts: $e")
        }
        return hashSetOfAddress
    }
    companion object{
        const val TAG ="__HashWorker"
    }

}