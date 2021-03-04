package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import ContactRepository
import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.utils.ContactRepositoryTwo

object UserInfoInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val contactsRepository = ContactRepositoryTwo(context)


        val userNetworkRepository = UserNetworkRepository(
            context,
            userInfoDAO,
            senderInfoFromServerDAO
        )

        return UserViewModelFactory(
            userNetworkRepository, contactsRepository
        )
    }
}