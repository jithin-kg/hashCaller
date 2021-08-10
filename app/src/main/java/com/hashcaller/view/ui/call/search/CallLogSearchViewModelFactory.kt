package com.hashcaller.view.ui.call.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Created by Jithin KG on 29,July,2020
 */
class CallLogSearchViewModelFactory(
    private val repository: CallLogSearchRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return CallLogSearchViewModel(repository) as T
    }
}