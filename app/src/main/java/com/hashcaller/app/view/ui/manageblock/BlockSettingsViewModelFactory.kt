package com.hashcaller.app.view.ui.manageblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Jithin KG on 29,July,2020
 */
class BlockSettingsViewModelFactory():
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return BlockSettingsViewModel(  ) as T
    }
}