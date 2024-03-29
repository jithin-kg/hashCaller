package com.hashcaller.app.view.ui.contacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.repository.contacts.ContactLocalSyncRepository
import com.hashcaller.app.repository.contacts.ContactsNetworkRepository
import com.hashcaller.app.repository.search.ContactSearchRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
class ContactsViewModelFactory(
    private val contactLiveData: ContactLiveData,
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val contactsRepository: ContactSearchRepository?,
    private val contactNetworkRepository: ContactsNetworkRepository?

):
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return ContactsViewModel(contactLiveData,
            contactLocalSyncRepository,
            contactsRepository,
            contactNetworkRepository
        ) as T
    }
}