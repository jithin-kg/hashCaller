package com.nibble.hashcaller.view.ui.hashworker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel

class HashViewmodelFactory(private val repository:HashedDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HasherViewmodel(repository) as T
    }
}