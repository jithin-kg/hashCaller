package com.hashcaller.view.ui.contacts.search

import androidx.lifecycle.*
import com.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.hashcaller.repository.search.SearchNetworkRepository
import com.hashcaller.stubs.Contact


/**
 * Created by Jithin KG on 22,July,2020
 */
class FullSearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository,
    private  val contactLocalSyncRepository: ContactLocalSyncRepository
): ViewModel() {

    init{
    }


    fun searchContactsInDb(phoneNumber: String):LiveData<List<Contact>> = liveData{

        if(!phoneNumber.trim().equals(""))  {
            val c = contactLocalSyncRepository.getContactsLike(phoneNumber) as List<Contact>
//            Log.d(TAG, "size is ${c?.size} ")
//            contactsLocalSearchLiveDAta.value = c
            emit(c)
        }
//        else{
//            contactsLocalSearchLiveDAta.value = emptyList()
//        }



    }



companion object{
    private const val TAG ="__SearchViewModel"
}
}
