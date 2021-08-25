package com.hashcaller.app.view.ui.IncommingCall

import android.content.Context
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.utils.notifications.tokeDataStore

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = SpamNetworkRepository(context, spamListDAO, DataStoreRepository(context.tokeDataStore))
        return IncommingCallViewModelFactory(spamNetworkRepository)
    }
}