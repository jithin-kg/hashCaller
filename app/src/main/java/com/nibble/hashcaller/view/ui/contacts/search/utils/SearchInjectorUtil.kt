package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository

object SearchInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : SearchViewModelFactory {
        val searchNetworkRepository = SearchNetworkRepository(context)
        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO)

        return SearchViewModelFactory(searchNetworkRepository, contactLocalSyncRepository)
    }
}