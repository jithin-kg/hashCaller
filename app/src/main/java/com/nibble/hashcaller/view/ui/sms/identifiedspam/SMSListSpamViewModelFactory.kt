package com.nibble.hashcaller.view.ui.sms.identifiedspam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSListSpamViewModelFactory(
    private val SMSLiveData: SMSSpamLiveData?,
    private val repository: SMSLocalRepository?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSSpamViewModel(this!!.SMSLiveData!!, repository) as T
    }
}