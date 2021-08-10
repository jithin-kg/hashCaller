package com.hashcaller.view.ui.sms.spam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Created by Jithin KG on 29,July,2020
 */
class SpamSMSViewmodelFactory(private val repository: SpamSMSRepository) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return SpamSMSViewModel(repository) as T
    }
}