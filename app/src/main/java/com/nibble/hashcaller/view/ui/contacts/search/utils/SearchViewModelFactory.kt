package com.nibble.hashcaller.view.ui.contacts.search.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.IncommingCall.LocalDbSearchRepository

class SearchViewModelFactory(
    private val searchNetworkRepository: SearchNetworkRepository,
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val localDbSearchRepository: LocalDbSearchRepository,
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchViewModel(searchNetworkRepository,
            contactLocalSyncRepository,
            localDbSearchRepository
            ) as T
    }
}