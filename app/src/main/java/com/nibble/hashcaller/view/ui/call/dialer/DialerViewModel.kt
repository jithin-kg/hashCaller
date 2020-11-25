package com.nibble.hashcaller.view.ui.call.dialer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class DialerViewModel(val callLogs:CallLogLiveData): ViewModel() {
    private var phoneNumber: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (phoneNumber == null) {
            phoneNumber = MutableLiveData<String>()
            return phoneNumber
        }
        return phoneNumber

    }

    fun getCallHistory() {

    }


    companion object{
        private const val TAG ="__DialerViewModel"
    }
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}