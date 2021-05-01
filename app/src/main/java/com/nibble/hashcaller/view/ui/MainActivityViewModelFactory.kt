package com.nibble.hashcaller.view.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository

class MainActivityViewModelFactory(
    private val userNetworkRepository: UserNetworkRepository,
    private val userHasehdNumRepository: UserHasehdNumRepository
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserInfoViewModel(
            userNetworkRepository,
            userHasehdNumRepository
        ) as T
    }
}