package com.nibble.hashcaller.datastore

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DataStoreManager {

    val Context.tokeDataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

    suspend fun saveTken(context: Context, key:String, value:String){
       val wrapedKey =  stringPreferencesKey(key)
       context.tokeDataStore.edit {
           it[wrapedKey] = value
       }
    }

    suspend fun getToken(context: Context, key:String): String {
        val wrapedKey =  stringPreferencesKey(key)
        val tokenFlow:Flow<String> = context.tokeDataStore.data.map {
            it[wrapedKey]?:""
        }
        return tokenFlow.first()
    }
}