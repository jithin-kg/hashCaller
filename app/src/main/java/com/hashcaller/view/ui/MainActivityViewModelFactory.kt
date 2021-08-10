package com.hashcaller.view.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.repository.user.UserNetworkRepository
import com.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository

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