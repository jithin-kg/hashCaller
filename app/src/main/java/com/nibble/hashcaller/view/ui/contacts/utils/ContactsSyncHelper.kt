package com.nibble.hashcaller.view.ui.contacts.utils

import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.PhoneNumWithHashedNumDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository

import kotlin.collections.ArrayList

/**
 * Created by Jithin KG on 03,August,2020
 * This class helps to compare contacts list recieved from content provider and contact that are available
 * in sqlite database
 */
class ContactsSyncHelper(
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val contactNetworkRepository: ContactsNetworkRepository?
) {
    suspend fun syncContacts(
        contactsListfromContentProvider: ArrayList<PhoneNumWithHashedNumDTO>?,
        contactsListFromLocalDb: LiveData<List<ContactTable>>?
    ) {

        /**
         * create a map of contacts from localsqlite db
         * and remove contact common in local db and contact from content provider
         * here the key is number and value is the object itself
         */

//        val mapContactLocalDb = contactsListFromLocalDb?.associateBy({it.number}, {it})
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//            if (mapContactLocalDb != null) {
//                if(contactsListFromLocalDb.size?:0 < contactsListfromContentProvider?.size?:0){
//                    contactsListfromContentProvider?.removeIf{ contact-> mapContactLocalDb.containsKey(contact.phoneNumber)}
//
//                    if (contactsListfromContentProvider?.size == 0) Log.d(TAG, "nothing to sync: ")
//
//                    //insert the new contacts to database
//                    if (contactsListfromContentProvider != null) {
//
//                        for (item in contactsListfromContentProvider){
//                            var contact = ContactTable(null, item.phoneNumber, item.name)
//                            //insert new contacts
//                            contactLocalSyncRepository.insertContacts(contact)
//                            Log.d(TAG, "inserting ${contact}")
//                            //save the same contacts to server
//
//                            contactNetworkRepository?.uploadContacts(contactsListfromContentProvider)
//
//                        }
//
//                    }
//                }
//
//            }
//
//        }else{
//            //TODO for api level 23/ M
//        }


    }
    companion object{
        private const val TAG ="__ContactsSyncHelper"
    }

}