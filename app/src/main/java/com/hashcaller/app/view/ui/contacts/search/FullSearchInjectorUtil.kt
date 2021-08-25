package com.hashcaller.app.view.ui.contacts.search

import android.content.Context

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