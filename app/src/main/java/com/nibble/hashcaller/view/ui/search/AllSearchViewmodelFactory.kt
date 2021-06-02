package com.nibble.hashcaller.view.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AllSearchViewmodelFactory(private val repo: AllSearchRepository) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return AllSearchViewmodel(repo  ) as T
    }
}