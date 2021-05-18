package com.nibble.hashcaller.datastore

import android.content.Context
import com.nibble.hashcaller.utils.notifications.blockPreferencesDataStore
import com.nibble.hashcaller.utils.notifications.tokeDataStore

object DataStoreInjectorUtil {

    fun providerViewmodelFactory(context: Context) : DataStoreViewmodelFactory{

        val repository = DataStoreRepository(context.blockPreferencesDataStore)
        return DataStoreViewmodelFactory(repository)
    }
}