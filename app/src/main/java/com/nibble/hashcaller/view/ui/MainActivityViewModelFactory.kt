package com.nibble.hashcaller.view.ui

import ContactRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.utils.ContactRepositoryTwo

class MainActivityViewModelFactory(
    private val userNetworkRepository: UserNetworkRepository
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserInfoViewModel(
            userNetworkRepository
        ) as T
    }
}