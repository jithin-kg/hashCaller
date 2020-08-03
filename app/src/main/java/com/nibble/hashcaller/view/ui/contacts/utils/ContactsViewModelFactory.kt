package com.nibble.hashcaller.view.ui.contacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
class ContactsViewModelFactory(
    private val contactLiveData: ContactLiveData,
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val contactsRepository: ContactSearchRepository?

):
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return ContactsViewModel(contactLiveData, contactLocalSyncRepository, contactsRepository ) as T
    }
}