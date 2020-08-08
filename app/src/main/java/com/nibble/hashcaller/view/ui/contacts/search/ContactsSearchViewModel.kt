package com.nibble.hashcaller.view.ui.contacts.search

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 31,July,2020
 */
class ContactsSearchViewModel(application: Application): AndroidViewModel(application) {

    var contacts: MutableLiveData<List<ContactUploadDTO>>
    val contactSearchRepository = ContactSearchRepository(getApplication())
    init {


        contacts = contactSearchRepository.fetchContactsLiveData("")


    }

    var contactsList = mutableListOf<ContactUploadDTO>()
        @SuppressLint("LongLogTag")
    fun findContactForNum(number:String) = viewModelScope.launch(
            Dispatchers.IO) {


         contacts =  contactSearchRepository.fetchContactsLiveData(number)


    }
//    fun getContactsList(): LiveData<List<SearchContactSTub>> {
//    contacts = MutableLiveData<List<SearchContactSTub>>()
//       return contacts
//    }



}