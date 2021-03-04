package com.nibble.hashcaller.view.ui

import ContactRepository
import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.utils.ContactRepositoryTwo

object MainActivityInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : MainActivityViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val contactsRepository = ContactRepositoryTwo(context)
        val userNetworkRepository = UserNetworkRepository(context, userInfoDAO, senderInfoFromServerDAO)

        return MainActivityViewModelFactory(
            userNetworkRepository, contactsRepository
        )
    }
}