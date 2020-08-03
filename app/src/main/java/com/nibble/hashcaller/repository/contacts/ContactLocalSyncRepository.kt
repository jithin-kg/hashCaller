package com.nibble.hashcaller.repository.contacts

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO

/**
 * Created by Jithin KG on 01,August,2020
 */
class ContactLocalSyncRepository(
    private val contactLisDAO: IContactIformationDAO?
) {
     fun getCount(): LiveData<Int>? {
       return contactLisDAO?.getCount()
    }

    suspend fun getContactsFromLocalDB(): List<ContactTable>? {
        return contactLisDAO?.getContacts()
    }

    @SuppressLint("LongLogTag")
    suspend fun insertContacts(preparedContacts: ContactTable): Long? {
        Log.d(TAG, "insertContacts: ")
        val insert = contactLisDAO?.insert(preparedContacts)
        return insert;
    }



    companion object{
        private const val TAG = "__ContactLocalSyncRepository"
    }

}