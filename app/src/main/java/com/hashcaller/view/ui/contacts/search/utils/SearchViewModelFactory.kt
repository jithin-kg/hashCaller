package com.hashcaller.view.ui.contacts.search.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.repository.BlockListPatternRepository
import com.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.hashcaller.repository.search.SearchNetworkRepository
import com.hashcaller.view.ui.IncommingCall.LocalDbSearchRepository
import com.hashcaller.view.utils.LibPhoneCodeHelper

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
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            blockListPatternRepository
                     ) as T
    }
}