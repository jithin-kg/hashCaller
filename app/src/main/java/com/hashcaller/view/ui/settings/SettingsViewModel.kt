package com.hashcaller.view.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 *
 */
class SettingsViewModel(
  private val repository:SettingRepository
): ViewModel() {

    companion object{
        private const val TAG ="__SettingsViewModel"

    }

    fun getUserInfo() = viewModelScope.launch {

    }
}
