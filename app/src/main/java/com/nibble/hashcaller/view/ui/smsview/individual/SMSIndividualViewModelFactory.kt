package com.nibble.hashcaller.view.ui.smsview.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.smsview.util.SMSLocalRepository


/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSIndividualViewModelFactory(
    private val SMSLiveData: SMSIndividualLiveData?,
    private val repository: SMSLocalRepository?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSIndividualViewModel(this!!.SMSLiveData!!, repository) as T
    }
}