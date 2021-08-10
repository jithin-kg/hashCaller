package com.hashcaller.datastore

import android.content.Context
import com.hashcaller.utils.notifications.blockPreferencesDataStore

object DataStoreInjectorUtil {

    fun providerViewmodelFactory(context: Context) : DataStoreViewmodelFactory{

        val repository = DataStoreRepository(context.blockPreferencesDataStore)
        return DataStoreViewmodelFactory(repository)
    }
}