package com.nibble.hashcaller.work

import ContactRepository
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.load.HttpException
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
            Log.d(TAG, "doWork: ")

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
        contactsNetworkRepository.uploadContacts(contacts)

    }
    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }

}