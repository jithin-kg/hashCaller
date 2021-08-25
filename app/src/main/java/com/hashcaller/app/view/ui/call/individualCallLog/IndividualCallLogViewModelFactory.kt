package com.hashcaller.app.view.ui.call.individualCallLog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Created by Jithin KG on 29,July,2020
 */
class IndividualCallLogViewModelFactory(
    private val repository: IndividualCallhistoryRepository,
    private val callLogLiveData: IndividualCallLivedata
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return IndividualCallViewModel(repository,callLogLiveData ) as T
    }
}