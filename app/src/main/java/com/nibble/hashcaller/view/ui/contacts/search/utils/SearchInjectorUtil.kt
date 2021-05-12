package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.IncommingCall.LocalDbSearchRepository

object SearchInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : SearchViewModelFactory {
        val searchNetworkRepository = SearchNetworkRepository(TokenManager( DataStoreRepository(context.tokeDataStore)))
        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)
        val callersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        val localDbSearchRepository = LocalDbSearchRepository(callersInfoFromServerDAO)
        return SearchViewModelFactory(searchNetworkRepository, contactLocalSyncRepository, localDbSearchRepository)
    }
}