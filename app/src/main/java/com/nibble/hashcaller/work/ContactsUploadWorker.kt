package com.nibble.hashcaller.work

import ContactRepository
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository

/**
 * Created by Jithin KG on 25,July,2020
 * Todo update worker https://www.youtube.com/watch?v=6manrgTPzyA
 */
class ContactsUploadWorker(private val context: Context,private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<ContactUploadDTO>()
//    context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO()

    private val contactLisDAO:IContactIformationDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    private val contactLocalSyncRepository = ContactLocalSyncRepository(contactLisDAO)

    override suspend fun doWork(): Result {
        try {


                uploadContactsToServer()

        }catch (e:HttpException){
            return Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return Result.success()
    }

    private suspend fun uploadContactsToServer() {
        val contactRepository = ContactRepository(context)
        contacts.addAll(contactRepository.fetchContacts())
        val contactsNetworkRepository = ContactsNetworkRepository(context)
        val result = contactsNetworkRepository.uploadContacts(contacts)
        Log.d(TAG, "result:$result")
        Log.d(TAG, "body:${result?.body()}")
        val cntcts = result?.body()?.cntcts
        //save the contacts in the local db
        saveContactsToLocalDB(cntcts)



    }

    private suspend fun saveContactsToLocalDB(cntactsFromServer: List<Cntct>?) {

        var cts:MutableList<ContactTable>? = mutableListOf();

        if (cntactsFromServer != null) {
            for(item in cntactsFromServer){
                cts?.add(ContactTable(null, item.phoneNumber, item.name,
                    item.carrier,item.location, item.country, item.spammerStatus.spamCount))
            }
        }

        contactLocalSyncRepository.insertContacts(cts!!)
    }

    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}