package com.nibble.hashcaller.view.ui.call.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData

/**
 * Created by Jithin KG on 29,July,2020
 */
class DialerViewModelFactory(private val callLogLiveData: CallLogLiveData?) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return DialerViewModel(this!!.callLogLiveData!!) as T
    }
}