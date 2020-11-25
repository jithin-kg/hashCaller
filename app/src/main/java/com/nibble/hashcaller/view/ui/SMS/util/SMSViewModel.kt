package com.nibble.hashcaller.view.ui.SMS.util

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {
     var filteredSms: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms

    }



    fun search(searchQuery: String?)  = viewModelScope.launch{
      val sms =  repository?.getSms(searchQuery)
        SMS.value = sms

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