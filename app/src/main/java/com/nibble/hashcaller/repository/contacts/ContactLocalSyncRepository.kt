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

    var  contactsFomLocalDB = contactLisDAO?.getContacts()

    suspend fun getContacts(phonNumber:String): List<ContactTable>? {
        val res = contactLisDAO?.search(phonNumber)
//        val res = contactLisDAO?.search()
//        contactsFomLocalDB = res
       return  res
    }

    @SuppressLint("LongLogTag")
    suspend fun insertContacts(preparedContacts: List<ContactTable>) {
        Log.d(TAG, "insertContacts: ")
        val insert = contactLisDAO?.insert(preparedContacts)
        Log.d(TAG, "insertContacts:$insert ")

    }

    suspend fun insertSingleContactItem(c: ContactTable) {
    contactLisDAO!!.insertSingleItem(c)
    }


    companion object{
        private const val TAG = "__ContactLocalSyncRepository"
    }

}