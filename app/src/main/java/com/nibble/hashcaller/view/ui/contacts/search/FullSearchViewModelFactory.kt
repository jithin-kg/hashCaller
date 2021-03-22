package com.nibble.hashcaller.view.ui.contacts.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository

class FullSearchViewModelFactory(
    private val searchNetworkRepository: SearchNetworkRepository,
    private  val contactLocalSyncRepository: ContactLocalSyncRepository
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FullSearchViewModel(searchNetworkRepository, contactLocalSyncRepository) as T
    }
}