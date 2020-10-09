package com.nibble.hashcaller.view.ui.IncommingCall

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamNetworkRepository = SpamNetworkRepository(context)



        return IncommingCallViewModelFactory(spamNetworkRepository)
    }
}