package com.hashcaller.app.datastore

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hashcaller.app.utils.auth.EnCryptor
import com.hashcaller.app.view.ui.contacts.utils.SAMPLE_ALIAS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class DataStoreRepository(private val dataStore: DataStore<Preferences>)  {


        suspend fun saveTken( key:String, value:String){

            val wrapedKey =  stringPreferencesKey(key)
            dataStore.edit {
                it[wrapedKey] = value
            }
        }

    suspend fun savePreferencesBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO){
        val wrapedKey = booleanPreferencesKey(key)
        dataStore.edit {
            it[wrapedKey] = value
        }
    }
    suspend fun getSharedPreferencesBoolean(key: String): Boolean {
        val wrapedKey =  booleanPreferencesKey(key)
        val tokenFlow:Flow<Boolean> = dataStore.data.map {
            it[wrapedKey]?:false
        }

        return tokenFlow.first()
    }

        suspend fun getToken(tokeDataStore: DataStore<Preferences>, key:String): String {
            val wrapedKey =  stringPreferencesKey(key)
            val tokenFlow:Flow<String> = tokeDataStore.data.map {
                it[wrapedKey]?:""
            }
            return tokenFlow.first()
        }
    suspend fun getToken(): String  = withContext(Dispatchers.IO){
        return@withContext getToken(dataStore,  PreferencesKeys.TOKEN)
    }
//
//    suspend fun saveToken(encodeTokenString: String) = withContext(Dispatchers.IO) {
////       saveTken( PreferencesKeys.TOKEN, encodeTokenString)
//    }

    suspend fun getEncryptedStr(token: String): String = withContext(Dispatchers.IO){
        val encryptor = EnCryptor()
        val encryptedText = encryptor?.encryptText(SAMPLE_ALIAS,token.toString())
        val encodeTokenString = Base64.encodeToString(
            encryptedText,
            Base64.DEFAULT
        )
        return@withContext encodeTokenString?:""
    }

    suspend fun setBoolean(value: Boolean, key: String)  = withContext(Dispatchers.IO) {
        val wrapedKey = booleanPreferencesKey(key)
        dataStore.edit {
            it[wrapedKey] = value
        }
    }

    suspend fun getBoolean(key: String): Boolean = withContext(Dispatchers.IO) {
        val wrapedKey =  booleanPreferencesKey(key)
        val tokenFlow:Flow<Boolean> = dataStore.data.map {
            it[wrapedKey]?:false
        }
        return@withContext tokenFlow.first()
    }

     fun getBooleanFlow(key: String): Flow<Boolean> {
        val wrapedKey =  booleanPreferencesKey(key)
        val tokenFlow:Flow<Boolean> = dataStore.data.map {
            it[wrapedKey]?:false
        }
        return tokenFlow
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