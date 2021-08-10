package com.hashcaller.datastore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DataStoreViewmodelFactory(private val repository: DataStoreRepository) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return DataStoreViewmodel(repository) as T
    }
}