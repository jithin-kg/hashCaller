package com.hashcaller.app.datastore

object DataStoreManager {

//    val Context.tokeDataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)
//
//    suspend fun saveTken(context: Context, key:String, value:String){
//       val wrapedKey =  stringPreferencesKey(key)
//       context.tokeDataStore.edit {
//           it[wrapedKey] = value
//       }
//    }
//
//    suspend fun getToken(context: Context, key:String): String {
//        val wrapedKey =  stringPreferencesKey(key)
//        val tokenFlow:Flow<String> = context.tokeDataStore.data.map {
//            it[wrapedKey]?:""
//        }
//        return tokenFlow.first()
//    }
}