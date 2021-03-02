package com.nibble.hashcaller.work

import ContactRepository
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.Secrets

import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.contactInformation.ContactLastSyncedDate
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.local.db.contactInformation.IContactLastSycnedDateDAO
import com.nibble.hashcaller.network.contact.ContactUploadResponseItem
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.contacts.ContactsSyncDTO
import retrofit2.HttpException
import java.util.*

/**
 * Created by Jithin KG on 25,July,2020
 * Todo update worker https://www.youtube.com/watch?v=6manrgTPzyA
 */
class ContactsUploadWorker(private val context: Context,private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<ContactUploadDTO>()
    private  var contactsListOf12: List<List<ContactUploadDTO>> = mutableListOf()
//    context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO()

    private val contactLisDAO:IContactIformationDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    private val contactsLastSyncedDateDAO:IContactLastSycnedDateDAO = HashCallerDatabase.getDatabaseInstance(context).contactLastSyncedDateDAO()
    private val contactLocalSyncRepository = ContactLocalSyncRepository(contactLisDAO)

    override suspend fun doWork(): Result {
        try {

            val lastDate = contactsLastSyncedDateDAO.getLastSyncedDate()

            setNewlySavedContactsList()
            if(!contactsListOf12.isNullOrEmpty()){

                for (contactSublist in contactsListOf12){
                    Log.d("__size", "doWork: sublist size is ${contactSublist.size}")
                    val countryCode =   "91" //for emulator country code should be 91
//            val countryISO = countryCodeHelper.getCountryISO()
                    val countryISO = "IN" //for testing in emulator coutry iso should be india otherwise it always returns us

                    val contactSyncDto = ContactsSyncDTO(contactSublist, countryCode.toString(), countryISO)
                    val contactsNetworkRepository = ContactsNetworkRepository(context)
                    val result = contactsNetworkRepository.uploadContacts(contactSyncDto)
                    Log.d(TAG, "result:$result")
                    Log.d(TAG, "body:${result?.body()}")
                    val cntcts = result?.body()?.cntcts
                    Log.d("__size", "doWork: result list size is ${cntcts!!.size}")
                    saveContactsToLocalDB(cntcts)
                    saveDateInContactLastSycnDate()
                }
            }

//            if(lastDate==null ){
//               sendContactsToServer()
//            }else{
//                val date = lastDate.date;
//                val dateCompareHelper = DateCompareHelper()
//                if(dateCompareHelper.isSyncDateLimitReached(date, 7)){
//                    sendContactsToServer()
//                }
//            }
            //if the previous contact synced date is greater than 7 perform the work

            Log.d(TAG, "doWork:")
//                uploadContactsToServer()


        }catch (e: HttpException){
            return Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return Result.success()
    }

    private suspend fun setNewlySavedContactsList() {
        val contactRepository = ContactRepository(context)
        val newlyCreatedContacts = mutableListOf<ContactUploadDTO>()
        val allcontactsInContentProvider = contactRepository.fetchContacts()

        for(contact in allcontactsInContentProvider){

            if(!contact.phoneNumber.isNullOrEmpty()){
                val formattedPhoneNum = formatPhoneNumber(contact.phoneNumber)
                val res = contactLocalSyncRepository.getContact(formattedPhoneNum)
                if(res==null){
                    Log.d("__NOTINDB", "$formattedPhoneNum")
                    val hashedPhoneNum = Secrets().managecipher(context.packageName, formattedPhoneNum)
                    val cntctDtoObj = ContactUploadDTO(contact.name, contact.phoneNumber, hashedPhoneNum)
                    contacts.add(cntctDtoObj)
                }
            }
        }
        contactsListOf12 = contacts.chunked(12)
    }

    
    private suspend fun saveContactsToLocalDB(cntactsFromServer: List<ContactUploadResponseItem>?) {
        Log.d(TAG, "saveContactsToLocalDB:  ")
        var cts:MutableList<ContactTable>? = mutableListOf();

        if (cntactsFromServer != null) {
            for(item in cntactsFromServer){
                Log.d(TAG, "saveContactsToLocalDB: inserting ${item}")
                val c = ContactTable(null, item.phoneNumber, item.name,
                    item.carrier,item.location, "india", item.spamCount)
                contactLocalSyncRepository.insertSingleContactItem(c)
                cts?.add(c)
            }
        }
        Log.d(TAG, "saveContactsToLocalDB: inserting ${cts}")
        Log.d(TAG, "saveContactsToLocalDB: inserting size is  ${cts!!.size}")
        contactLocalSyncRepository.insertContacts(cts!!)
    }

    private suspend fun saveDateInContactLastSycnDate() {
        this.contactsLastSyncedDateDAO.delteAll()
        this.contactsLastSyncedDateDAO.insert(ContactLastSyncedDate(null, Date()))
    }

//    private suspend fun sendContactsToServer() {
//        val contactRepository = ContactRepository(context)
//        contacts.addAll(contactRepository.fetchContacts())
//        val countryCodeHelper = CountrycodeHelper(context)
////            val countryCode =   countryCodeHelper.getCountrycode()
//        val countryCode =   "91" //for emulator country code should be 91
////            val countryISO = countryCodeHelper.getCountryISO()
//        val countryISO = "IN" //for testing in emulator coutry iso should be india otherwise it always returns us
//
//        val contactSyncDto = ContactsSyncDTO(contacts, countryCode.toString(), countryISO)
//        val contactsNetworkRepository = ContactsNetworkRepository(context)
//        val result = contactsNetworkRepository.uploadContacts(contactSyncDto)
//        Log.d(TAG, "result:$result")
//        Log.d(TAG, "body:${result?.body()}")
//        val cntcts = result?.body()?.cntcts
////        saveContactsToLocalDB(cntcts)
//        saveDateInContactLastSycnDate()
//    }
    private suspend fun uploadContactsToServer() {
//        val contactRepository = ContactRepository(context)
//        contacts.addAll(contactRepository.fetchContacts())
//        val contactsNetworkRepository = ContactsNetworkRepository(context)
//        val result = contactsNetworkRepository.uploadContacts(contacts)
//        Log.d(TAG, "result:$result")
//        Log.d(TAG, "body:${result?.body()}")
//        val cntcts = result?.body()?.cntcts
//        //save the contacts in the local db
//        saveContactsToLocalDB(cntcts)



    }

   

    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}