package com.nibble.hashcaller.view.ui.IncommingCall

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.HashCallerDatabase_Impl
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = SpamNetworkRepository(context, spamListDAO)



        return IncommingCallViewModelFactory(spamNetworkRepository)
    }
}