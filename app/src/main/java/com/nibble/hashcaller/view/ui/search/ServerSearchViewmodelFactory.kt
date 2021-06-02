package com.nibble.hashcaller.view.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.search.SearchNetworkRepository

class ServerSearchViewmodelFactory(private val searchNetworkRepository: SearchNetworkRepository) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return ServerSearchViewModel(searchNetworkRepository  ) as T
    }
}