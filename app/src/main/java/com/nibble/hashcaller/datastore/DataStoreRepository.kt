package com.nibble.hashcaller.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class DataStoreRepository(private val context: Context)  {

    suspend fun getToken(): String  = withContext(Dispatchers.IO){
        return@withContext DataStoreManager.getToken(context,  PreferencesKeys.TOKEN)

    }

    suspend fun saveToken(encodeTokenString: String) = withContext(Dispatchers.IO) {
        DataStoreManager.saveTken(context, PreferencesKeys.TOKEN, encodeTokenString)
    }
//    suspend fun saveToken( key:String, value:String){
//        val wrapedKey =  stringPreferencesKey(key)
//        context.tokeDataStore.edit {
//            it[wrapedKey] = value
//        }
//    }
//
//    suspend fun getToken( key:String): String {
//        val wrapedKey =  stringPreferencesKey(key)
//        val tokenFlow: Flow<String> = context.tokeDataStore.data.map {
//            it[wrapedKey]?:""
//        }
//        return tokenFlow.first()
//    }
}