package com.hashcaller.view.ui.auth.getinitialInfos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.repository.user.UserNetworkRepository
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository

class UserViewModelFactory(
    private val userNetworkRepository: UserNetworkRepository,
    private val userHashedNumRepository: UserHasehdNumRepository
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserInfoViewModel(
            userNetworkRepository,
            userHashedNumRepository
        ) as T
    }
}