package com.nibble.hashcaller.view.ui.auth.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel

class UserViewModelFactory(private val userNetworkRepository: UserNetworkRepository)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UserInfoViewModel(userNetworkRepository) as T
    }
}