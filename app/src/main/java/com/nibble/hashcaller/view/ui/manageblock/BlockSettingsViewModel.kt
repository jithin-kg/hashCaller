package com.nibble.hashcaller.view.ui.manageblock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class BlockSettingsViewModel(): ViewModel() {
    companion object{
        private const val TAG ="__BlockSettingsViewModel"
    }
}
