package com.nibble.hashcaller.datastore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.KEY_BLOCK_COMMONG_SPAMMERS
import kotlinx.coroutines.launch
import java.lang.Exception

class DataStoreViewmodel(private val repository: DataStoreRepository) : ViewModel() {
    fun getToken():LiveData<String> = liveData {
       emit(repository.getToken())
    }

    fun enableBlockCommonSpammers(isChecked: Boolean) = viewModelScope.launch{
        repository?.savePreferencesBoolean(KEY_BLOCK_COMMONG_SPAMMERS, isChecked)
    }


    fun saveTokenViewmodelScope(encodeTokenString: String) = viewModelScope.launch {
//        repository.saveToken(encodeTokenString)
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