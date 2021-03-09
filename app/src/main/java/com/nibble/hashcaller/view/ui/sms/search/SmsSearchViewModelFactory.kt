package com.nibble.hashcaller.view.ui.sms.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel

/**
 * Created by Jithin KG on 29,July,2020
 */
class SmsSearchViewModelFactory(
    private val repository: SMSLocalRepository?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSSearchViewModel(repository) as T
    }
}