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
    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive

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

//        smsLive = SMSLiveData(context)
//        smsLive.value = smsLIst as List<SMS>
    }

    /**
     * @param smslist list of sms from content provider
     * function to get name for sms senders
     * if the address is a number(5555, 801238312) not a name('jio, vodafone-pay,etc')
     * then we need to get info for that number in locally
     * for address that is not in contact we need to search for that in server
     * even if that is of type number(5555, 801238312) or a name('jio, vodafone-pay,etc')
     *
     */
    fun getNameForUnknownSender(smslist: List<SMS>) = viewModelScope.launch {
        getInfoFromContacts(smslist)

    }

    private fun getInfoFromContacts(smslist: List<SMS>) {
        //I dont need to update smsLive.value because list is passed as reference,
        //since I am updating the list with name in getInfoFromContacts(smslist) from repository
        //automatically update the value
        //******Here list is passed as Reference ********
        repository!!.getInfoFromContacts(smslist)
    }


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}