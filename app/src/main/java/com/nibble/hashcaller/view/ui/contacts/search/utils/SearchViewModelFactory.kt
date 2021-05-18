package com.nibble.hashcaller.view.ui.contacts.search.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.IncommingCall.LocalDbSearchRepository
import com.nibble.hashcaller.view.utils.LibCoutryCodeHelper

class SearchViewModelFactory(
    private val searchNetworkRepository: SearchNetworkRepository,
    private val contactLocalSyncRepository: ContactLocalSyncRepository,
    private val localDbSearchRepository: LocalDbSearchRepository,
    private val blockListPatternRepository: BlockListPatternRepository,
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return SearchViewModel(searchNetworkRepository,
            contactLocalSyncRepository,
            localDbSearchRepository,
            LibCoutryCodeHelper(PhoneNumberUtil.getInstance()),
            blockListPatternRepository
                     ) as T
    }
}