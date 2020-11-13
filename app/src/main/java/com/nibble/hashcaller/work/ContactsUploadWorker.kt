package com.nibble.hashcaller.work

import ContactRepository
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository

/**
 * Created by Jithin KG on 25,July,2020
 */
class ContactsUploadWorker(private val context: Context,private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    val contacts = mutableListOf<ContactUploadDTO>()
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
        val contacts = result?.body()?.cntcts
        //save the contacts in the local db
        saveContactsToLocalDB(contacts)



    }

    private fun saveContactsToLocalDB(contacts: List<Cntct>?) {

        var cts:MutableList<ContactTable>? = mutableListOf();

        for(item in this.contacts){
            cts?.add(ContactTable(null, item?.phoneNumber, item?.name))
        }
    }

    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}