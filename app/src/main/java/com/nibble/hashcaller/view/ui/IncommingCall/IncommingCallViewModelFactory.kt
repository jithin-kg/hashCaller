package com.nibble.hashcaller.view.ui.IncommingCall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository

class IncommingCallViewModelFactory(
    private val spamNetworkRepository: SpamNetworkRepository

)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IncommingCallViewModel(spamNetworkRepository) as T
    }
}