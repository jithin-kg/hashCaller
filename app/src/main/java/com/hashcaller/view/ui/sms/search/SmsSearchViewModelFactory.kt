package com.hashcaller.view.ui.sms.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
class SmsSearchViewModelFactory(
    private val repository: SMSLocalRepository?,
    private  val smsSearchRepository: SMSSearchRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSSearchViewModel(repository, smsSearchRepository) as T
    }
}