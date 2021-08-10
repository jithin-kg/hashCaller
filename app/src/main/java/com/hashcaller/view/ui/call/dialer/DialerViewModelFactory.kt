package com.hashcaller.view.ui.call.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Jithin KG on 29,July,2020
 */
class DialerViewModelFactory(
    private val repository: DialerRepository?,
    private val contactSearchRepository: ContactSearchRepository?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return DialerViewModel(repository, contactSearchRepository) as T
    }
}