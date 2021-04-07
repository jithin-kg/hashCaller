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
import com.nibble.hashcaller.local.db.contacts.ContactAddresses
import com.nibble.hashcaller.network.contact.ContactUploadResponseItem
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.contacts.ContactsSyncDTO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

/**
 * This worker is used to save all contactaddress formated into db
 */
class ContactsAddressLocalWorker(private val context: Context, private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    private val contactAddressDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
    private var allcontactsInContentProvider:MutableList<ContactUploadDTO> = mutableListOf()
    override suspend fun doWork(): Result {
        try {
            val contactRepository = ContactRepository(context)
            allcontactsInContentProvider.addAll( contactRepository.fetchContacts())
            for (ct in allcontactsInContentProvider){
                contactAddressDAO.find(ct.phoneNumber).apply {
                    if(this == null){
                        contactAddressDAO.insert(ContactAddresses(ct.phoneNumber)).apply {

                        }
                    }
                }
            }

        }catch (e: HttpException){
            return Result.retry()
            Log.d(TAG, "doWork: retry")
        }
        return Result.success()
    }


   

    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}