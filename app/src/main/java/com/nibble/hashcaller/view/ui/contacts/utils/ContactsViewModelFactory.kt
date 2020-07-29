package com.nibble.hashcaller.view.ui.contacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
class ContactsViewModelFactory(private val contactLiveData: ContactLiveData):
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactsViewModel(contactLiveData) as T
    }
}