package com.nibble.hashcaller.view.ui.call.dialer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.stubs.Contact
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class DialerViewModel(
    private val repository: DialerRepository?,
    private val contactSearchRepository: ContactSearchRepository?
) : ViewModel() {
    private var phoneNumber: MutableLiveData<String>? = null
    var searchResultLivedata : MutableLiveData<MutableList<Contact>> = MutableLiveData()


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (phoneNumber == null) {
            phoneNumber = MutableLiveData<String>()
            return phoneNumber
        }
        return phoneNumber
    }



    fun getFirst10Logs() = viewModelScope.launch  {
       val result =  repository?.getFirst10Logs()

    }

    fun searchContactsInDb(phoneNumber: String) = viewModelScope.launch{
        searchResultLivedata.value =  contactSearchRepository?.getContactsLike(phoneNumber)
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