package com.nibble.hashcaller.view.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.call.dialer.DialerViewModel
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData

class CallviewModelFactory (private val callLogLiveData: CallLogLiveData?) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return CallHistoryViewmodel(this!!.callLogLiveData!!) as T
    }
}
