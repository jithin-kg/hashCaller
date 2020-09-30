package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.content.Context
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.repository.user.UserNetworkRepository

object SearchInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : SearchViewModelFactory {
        val searchNetworkRepository = SearchNetworkRepository(context)

        return SearchViewModelFactory(searchNetworkRepository)
    }
}