package com.nibble.hashcaller.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class DataStoreRepository(private val tokeDataStore: DataStore<Preferences>)  {
        suspend fun saveTken( key:String, value:String){
            val wrapedKey =  stringPreferencesKey(key)
            tokeDataStore.edit {
                it[wrapedKey] = value
            }
        }

        suspend fun getToken(tokeDataStore: DataStore<Preferences>, key:String): String {
            val wrapedKey =  stringPreferencesKey(key)
            val tokenFlow:Flow<String> = tokeDataStore.data.map {
                it[wrapedKey]?:""
            }
            return tokenFlow.first()
        }
    suspend fun getToken(): String  = withContext(Dispatchers.IO){
        return@withContext getToken(tokeDataStore,  PreferencesKeys.TOKEN)

    }

    suspend fun saveToken(encodeTokenString: String) = withContext(Dispatchers.IO) {
       saveTken( PreferencesKeys.TOKEN, encodeTokenString)
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