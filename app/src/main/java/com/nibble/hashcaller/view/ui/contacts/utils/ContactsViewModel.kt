package com.nibble.hashcaller.view.ui.contacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactsViewModel(
    val contacts: ContactLiveData,
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val contactsRepository: ContactSearchRepository?,
    private val contactNetworkRepository: ContactsNetworkRepository?
): ViewModel() {
    init {
//       syncContactsWithLocalDb()

    }

     fun syncContactsWithLocalDb() = viewModelScope.launch {

         val vowels: List<String> = ArrayList()

         val contactsListfromContentProvider: List<ContactUploadDTO>? = contactsRepository?.fetchContacts() as ArrayList<ContactUploadDTO>?
          var cts:MutableList<ContactTable>? = mutableListOf();
         for(item in contactsListfromContentProvider!!){
             if (cts != null) {
//                 cts.add(ContactTable(null, item?.phoneNumber, item?.name))
             }
           }
         contactLocalSyncRepository.insertContacts(cts!!)

//         val contactsListFromLocalDb = contactLocalSyncRepository.getContactsFromLocalDB()
         val contactHelper = ContactsSyncHelper(contactLocalSyncRepository, contactNetworkRepository)
//         contactHelper.syncContacts(contactsListfromContentProvider, contactsListFromLocalDb)

    }





    /**
     * If there is no contacts is local sqlite database then we insert all contacts
     * by adding all contacts to a list
     */
    private suspend fun insertContactstoLocalDb(contentProviderContacts: List<ContactUploadDTO>?) {
        val contactsListToSave:MutableList<ContactTable> = mutableListOf()
//        for(item in contentProviderContacts){
//            Log.d(TAG, "getPreparedContacts: ${i}")
//            val contact = ContactTable(item.phoneNumber, item.name)
//           contactLocalSyncRepository.insertContacts(contact)
//        }

//        contentProviderContacts?.forEachIndexed { i, item->
//            run {
//                val contact = ContactTable(null, item.phoneNumber, item.name)
//
//                val response = contactLocalSyncRepository.insertContacts(contact)
//            }
//        }
       
    }

//    private suspend fun getContactsFromLocalDB(): List<ContactTable>? {
//
////        val job = contactLocalSyncRepository.getContactsFromLocalDB()
////
////       return job
//
//    }

companion object{
    private const val TAG ="__ContactsViewModel"
}
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}