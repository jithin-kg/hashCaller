package com.hashcaller.app.work

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.contacts.ContactAddresses
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.util.*

/**
 * This worker is used to save all contactaddress formated into db
 */
class ContactsAddressLocalWorker(private val context: Context, private val params:WorkerParameters ) :
        CoroutineWorker(context, params){
    private val contactAddressDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
    private var allcontactsInContentProvider:MutableList<PhoneNumWithHashedNumDTO> = mutableListOf()
    override suspend fun doWork(): Result  = withContext(Dispatchers.IO) {
        try {
            val cursor = context!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

//            val contactRepository = WorkerContactRepository(
//                cursor,
//                countryCodeHelper.getCountryISO(),
//                libCountryHelper
//            )
//            allcontactsInContentProvider.addAll( contactRepository.fetchContacts())
            for (ct in allcontactsInContentProvider){
                contactAddressDAO.find(ct.phoneNumber).apply {
                    if(this == null){
                        contactAddressDAO.insert(ContactAddresses(ct.phoneNumber)).apply {
                        }
                    }
                }
            }

        }catch (e: HttpException){
            Log.d(TAG, "doWork: retry")
            return@withContext Result.retry()

        }
        return@withContext Result.success()
    }


   

    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}