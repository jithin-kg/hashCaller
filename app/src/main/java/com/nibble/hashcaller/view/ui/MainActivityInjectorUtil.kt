package com.nibble.hashcaller.view.ui

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserViewModelFactory

object MainActivityInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : MainActivityViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val userNetworkRepository = UserNetworkRepository(context, userInfoDAO)

        return MainActivityViewModelFactory(
            userNetworkRepository
        )
    }
}