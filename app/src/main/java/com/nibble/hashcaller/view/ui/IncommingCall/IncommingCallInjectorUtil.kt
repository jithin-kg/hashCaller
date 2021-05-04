package com.nibble.hashcaller.view.ui.IncommingCall

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.utils.notifications.tokeDataStore

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = SpamNetworkRepository(context, spamListDAO, DataStoreRepository(context.tokeDataStore))
        return IncommingCallViewModelFactory(spamNetworkRepository)
    }
}