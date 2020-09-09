package com.nibble.hashcaller.view.ui.auth.utils

import android.content.Context
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel

object UserInfoInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {
        val userNetworkRepository = UserNetworkRepository(context)

        return UserViewModelFactory(userNetworkRepository)
    }
}