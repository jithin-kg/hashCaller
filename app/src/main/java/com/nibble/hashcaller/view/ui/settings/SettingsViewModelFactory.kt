package com.nibble.hashcaller.view.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Jithin KG on 29,July,2020
 */
class SettingsViewModelFactory(private val repository: SettingRepository) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SettingsViewModel(repository) as T
    }
}