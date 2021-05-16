package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore

object FullSearchInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : FullSearchViewModelFactory? {
//        val searchNetworkRepository = SearchNetworkRepository(
//            TokenManager( DataStoreRepository(context.tokeDataStore)),
//            tokenHelper
//        )
//        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
//        val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)

//        return FullSearchViewModelFactory(searchNetworkRepository, contactLocalSyncRepository)
            return null
    }
}