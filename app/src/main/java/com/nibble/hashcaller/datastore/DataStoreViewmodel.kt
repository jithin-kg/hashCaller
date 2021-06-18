package com.nibble.hashcaller.datastore

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.KEY_BLOCK_COMMONG_SPAMMERS
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.SHOW_SMS_IN_SEARCH_RESULT
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    fun getPermissionAndUserInfo(key: String, context: Context):LiveData<Int> = liveData {
        var result = PERMISSION_AND_USER_INFO_NOT_GIVEN
        viewModelScope.launch{
         val permissionDef =    async {
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE

                )
            }

            val userInfoDef = async {  repository.getBoolean(key) }

            try {
                val isPermissionGiven = permissionDef.await()
                val isUserInfoAvialbleinDb = userInfoDef.await()

                if(isUserInfoAvialbleinDb && isPermissionGiven){
                  result = USER_INFO_AND_PERMISSION_GIVEN
                }
                else if(isUserInfoAvialbleinDb){
                    result = USER_INFO_ONLY_GIVEN
                }
                else if(isPermissionGiven){
                    result = PERMISSION__ONLY_GIVEN
                }
            }catch (e:Exception){
                Log.d(TAG, "getBooleanAndPermissionInfo: $e")
            }
        }.join()

        emit(result)

    }
    companion object{
        const val TAG = "__DataStoreViewmodel"

        const val USER_INFO_AND_PERMISSION_GIVEN = 1
        const val USER_INFO_ONLY_GIVEN = 2
        const val PERMISSION__ONLY_GIVEN = 3
        const val PERMISSION_AND_USER_INFO_NOT_GIVEN = 4
    }


}