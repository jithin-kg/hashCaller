//package com.nibble.hashcaller.view.ui.contacts
//
//import android.app.Application
//import androidx.databinding.ObservableArrayList
//import androidx.lifecycle.AndroidViewModel
//import com.nibble.hashcaller.repository.contacts.ContactRepository
//import com.nibble.hashcaller.stubs.Contact
//
///**
// * Created by Jithin KG on 21,July,2020
// */
//class ContactViewModel(application: Application): AndroidViewModel(application) {
//    private val contacts: ObservableArrayList<Contact>? = null
//    private val repository: ContactRepository? = null
//    private val TAG = "ContactViewModel"
//
//    fun getContacts(): List<Contact?>? {
//        repository?.fetchContacts()?.let { contacts?.addAll(it) }
//        return contacts
//    }
//}