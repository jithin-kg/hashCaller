package com.hashcaller.view.ui.IncommingCall

import android.content.Context
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.repository.spam.SpamNetworkRepository
import com.hashcaller.utils.notifications.tokeDataStore

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = SpamNetworkRepository(context, spamListDAO, DataStoreRepository(context.tokeDataStore))
        return IncommingCallViewModelFactory(spamNetworkRepository)
    }
}