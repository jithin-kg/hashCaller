package com.nibble.hashcaller.view.ui.contacts.search

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.stubs.Contact

/**
 * Created by Jithin KG on 31,July,2020
 */
class ContactsSearchViewModel(application: Application): AndroidViewModel(application) {
    val contacts = mutableListOf<Contact>()

    @SuppressLint("LongLogTag")
    fun findContactForNum(number:String): MutableList<ContactUploadDTO> {
        val contactSearchRepository = ContactSearchRepository(getApplication())

        val contacts =  contactSearchRepository.fetchContacts(number)
        Log.d("__ContactsSearchViewModel", "findContactForNum: ${contacts.size}")
        for (contact in contacts){
            Log.d("__ContactsSearchViewModel", "${contact.name} ")
            Log.d("__ContactsSearchViewModel", "${contact.phoneNumber} ")

        }
        return contacts
    }

}