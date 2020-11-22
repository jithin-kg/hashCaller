package com.nibble.hashcaller.view.ui.call.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel

/**
 * Created by Jithin KG on 29,July,2020
 */
class DialerViewModelFactory():
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return DialerViewModel() as T
    }
}