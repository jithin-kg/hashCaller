package com.nibble.hashcaller.view.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.work.ContactsAddressLocalWorker
import com.nibble.hashcaller.work.ContactsUploadWorker
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 *
 */
class SettingsViewModel(
  private val repository:SettingRepository
): ViewModel() {

    companion object{
        private const val TAG ="__SettingsViewModel"

    }

    fun getUserInfo() = viewModelScope.launch {

    }
}
