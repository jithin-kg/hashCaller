package com.hashcaller.app.datastore

import android.content.Context
import com.hashcaller.app.utils.notifications.blockPreferencesDataStore

object DataStoreInjectorUtil {

    fun providerViewmodelFactory(context: Context) : DataStoreViewmodelFactory{

        val repository = DataStoreRepository(context.blockPreferencesDataStore)
        return DataStoreViewmodelFactory(repository)
    }
}