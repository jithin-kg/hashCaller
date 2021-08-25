package com.hashcaller.app.view.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

class AllSearchViewmodelFactory(
    private val repo: AllSearchRepository,
    private val libPhoneCodeHelper: LibPhoneCodeHelper
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return AllSearchViewmodel(repo, libPhoneCodeHelper  ) as T
    }
}