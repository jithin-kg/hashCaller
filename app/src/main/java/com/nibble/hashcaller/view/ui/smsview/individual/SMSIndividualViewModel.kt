package com.nibble.hashcaller.view.ui.smsview.individual

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nibble.hashcaller.view.ui.smsview.util.SMSLocalRepository


/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSIndividualViewModel(
    val SMS: SMSIndividualLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {
    init {

    }
     var filteredSms: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms

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