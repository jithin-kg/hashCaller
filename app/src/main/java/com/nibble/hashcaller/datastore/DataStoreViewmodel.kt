package com.nibble.hashcaller.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DataStoreViewmodel(private val repository: DataStoreRepository) : ViewModel() {
    fun getToken():LiveData<String> = liveData {
       emit(repository.getToken())
    }

    fun saveToken(encodeTokenString: String) = viewModelScope.launch {
        repository.saveToken(  encodeTokenString)
    }

    override fun onCleared() {
        super.onCleared()
    }


}