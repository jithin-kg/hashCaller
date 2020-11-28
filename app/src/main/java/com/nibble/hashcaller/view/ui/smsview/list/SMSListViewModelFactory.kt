package com.nibble.hashcaller.view.ui.smsview.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.smsview.util.SMSLiveData
import com.nibble.hashcaller.view.ui.smsview.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.smsview.util.SMSViewModel

/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSListViewModelFactory(
    private val SMSLiveData: SMSLiveData?,
    private val repository: SMSLocalRepository?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSViewModel(this!!.SMSLiveData!!, repository) as T
    }
}