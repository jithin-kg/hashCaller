package com.nibble.hashcaller.datastore

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.SAMPLE_ALIAS
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.Exception

class DataStoreViewmodel(private val repository: DataStoreRepository) : ViewModel() {
    fun getToken():LiveData<String> = liveData {
       emit(repository.getToken())
    }

    fun saveToken(encodeTokenString: String) :LiveData<Int> = liveData {
        repository.saveToken(  encodeTokenString)
        emit(OPERATION_COMPLETED)
    }
    fun saveTokenViewmodelScope(encodeTokenString: String) = viewModelScope.launch {
        repository.saveToken(encodeTokenString)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun getEncryptedStr(token:String) :LiveData<String> = liveData{
        try {
            emit(repository.getEncryptedStr(token))

        }catch (e:Exception){
            Log.d(TAG, "getEncryptedStr: $e")
        }
    }
    companion object{
        const val TAG = "__DataStoreViewmodel"
    }


}