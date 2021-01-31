package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository

object UserInfoInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val userNetworkRepository = UserNetworkRepository(context, userInfoDAO)

        return UserViewModelFactory(
            userNetworkRepository
        )
    }
}