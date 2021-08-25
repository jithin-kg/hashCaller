package com.hashcaller.app.view.ui.hashworker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HashViewmodelFactory(private val repository:HashedDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HasherViewmodel(repository) as T
    }
}