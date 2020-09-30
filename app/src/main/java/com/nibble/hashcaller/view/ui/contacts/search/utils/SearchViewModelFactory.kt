package com.nibble.hashcaller.view.ui.contacts.search.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.search.SearchNetworkRepository

class SearchViewModelFactory(private val searchNetworkRepository: SearchNetworkRepository)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchViewModel(searchNetworkRepository) as T
    }
}