package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.notifications.tokeDataStore

object SearchInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : SearchViewModelFactory {
        val searchNetworkRepository = SearchNetworkRepository(context, DataStoreRepository(context.tokeDataStore))
        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)

        return SearchViewModelFactory(searchNetworkRepository, contactLocalSyncRepository)
    }
}