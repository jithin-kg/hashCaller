package com.nibble.hashcaller.datastore

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.KEY_BLOCK_COMMONG_SPAMMERS
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.SHOW_SMS_IN_SEARCH_RESULT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.Exception

class DataStoreViewmodel(private val repository: DataStoreRepository) : ViewModel() {
    val searchFilterLiveData: Flow<Boolean> = repository.getBooleanFlow(SHOW_SMS_IN_SEARCH_RESULT)

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

    fun setBoolean(key:String, value:Boolean)= viewModelScope.launch{
        repository.setBoolean(value, key)
    }

    fun getBoolean(key: String):LiveData<Boolean> = liveData {
        emit(repository.getBoolean(key))
    }
    companion object{
        const val TAG = "__DataStoreViewmodel"
    }


}