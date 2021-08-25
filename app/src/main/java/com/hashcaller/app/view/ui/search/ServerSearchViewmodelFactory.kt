package com.hashcaller.app.view.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

class ServerSearchViewmodelFactory(
    private val searchNetworkRepository: SearchNetworkRepository,
    private val libPhoneCodeHelper: LibPhoneCodeHelper
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return ServerSearchViewModel(searchNetworkRepository , libPhoneCodeHelper ) as T
    }
}