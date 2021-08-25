package com.hashcaller.app.view.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hashcaller.app.view.ui.call.dialer.util.CallLogLiveData

class CallHistoryViewmodel(val callLogs: CallLogLiveData): ViewModel() {
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
