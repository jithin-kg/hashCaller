package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import ContactRepository
import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.utils.ContactRepositoryTwo

object UserInfoInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }

        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val userNetworkRepository = UserNetworkRepository(
            TokenManager(sp),
            userInfoDAO,
            senderInfoFromServerDAO
        )

        return UserViewModelFactory(
            userNetworkRepository
        )
    }
}