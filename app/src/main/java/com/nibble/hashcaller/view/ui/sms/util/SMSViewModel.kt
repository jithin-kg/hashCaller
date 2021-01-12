package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {
    var smsLive:SMSLiveData = SMS
    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__DialerViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }



    var unreadMSCount:MutableLiveData<Int>? = null
     var filteredSms: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms

    }


    fun getInfoForPhoneNumbers(){

    }
    fun search(searchQuery: String?)  = viewModelScope.launch{
      val sms =  repository?.getSms(searchQuery)
        SMS.value = sms

    }

    fun getUnrealMsgCount() = viewModelScope.launch{
        val count = repository?.getUnreadMsgCount()
        unreadMSCount?.value = count
    }

    fun update(address: String)  = viewModelScope.launch{
     SMS?.update(address)
    }

    fun changelist(smsLIst: List<SMS>, context:Context)  = viewModelScope.launch{

        smsLive = SMSLiveData(context)
        smsLive.value = smsLIst as List<SMS>
    }


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}