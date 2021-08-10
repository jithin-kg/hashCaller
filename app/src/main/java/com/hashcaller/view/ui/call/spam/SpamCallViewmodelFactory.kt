package com.hashcaller.view.ui.call.spam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Created by Jithin KG on 29,July,2020
 */
class SpamCallViewmodelFactory(private val repository: SpamCallRepository) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return SpamCallViewModel(repository) as T
    }
}