package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Jithin KG on 22,July,2020
 */
class SearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository
): ViewModel() {
    // The internal MutableLiveData String that stores the most recent response
//    private val _response = MutableLiveData<String>()
//    var mt:
    // The external immutable LiveData for the response String
//    val response: LiveData<String>

//        get() = _response


//    init {
//       syncContactsWithLocalDb()
//
//    }

     fun syncContactsWithLocalDb() = viewModelScope.launch {

//
//         val contactsListfromContentProvider: ArrayList<ContactUploadDTO>? = contactsRepository?.fetchContacts() as ArrayList<ContactUploadDTO>?
//
//
//         val contactsListFromLocalDb = contactLocalSyncRepository.getContactsFromLocalDB()
//         val contactHelper = ContactsSyncHelper(contactLocalSyncRepository, contactNetworkRepository)
//         contactHelper.syncContacts(contactsListfromContentProvider, contactsListFromLocalDb)

    }

    fun search(phoneNumber:String) = liveData<String>(Dispatchers.IO) {

             try {
                  val res = searchNetworkRepository.search(phoneNumber)
//                 _response = "res"
                 Log.d(TAG, "search: ${res?.body()?.name}")
                 Log.d(TAG, "search: ${res?.body()?.message}")
                 Log.d(TAG, "search: ${res}")
                 emit(res?.body()?.name.toString());

             }catch (e:Exception){
                 Log.d(TAG, "search: $e")
             }


    }





    /**
     * If there is no contacts is local sqlite database then we insert all contacts
     * by adding all contacts to a list
     */
//    private suspend fun insertContactstoLocalDb(contentProviderContacts: List<ContactUploadDTO>?) {
//        val contactsListToSave:MutableList<ContactTable> = mutableListOf()
////        for(item in contentProviderContacts){
////            Log.d(TAG, "getPreparedContacts: ${i}")
////            val contact = ContactTable(item.phoneNumber, item.name)
////           contactLocalSyncRepository.insertContacts(contact)
////        }
//
//        contentProviderContacts?.forEachIndexed { i, item->
//            run {
//                val contact = ContactTable(null, item.phoneNumber, item.name)
//
//                val response = contactLocalSyncRepository.insertContacts(contact)
//            }
//        }
//
//    }

//    private suspend fun getContactsFromLocalDB(): List<ContactTable>? {
//
//        val job = contactLocalSyncRepository.getContactsFromLocalDB()
//
//       return job
//
//    }

companion object{
    private const val TAG ="__SearchViewModel"
}
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}