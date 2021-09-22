package com.hashcaller.app.datastore

import android.content.Context
import com.hashcaller.app.utils.notifications.tokeDataStore

object DataStoreInjectorUtil {

    fun providerViewmodelFactory(context: Context) : DataStoreViewmodelFactory{

        val repository = DataStoreRepository(context.tokeDataStore)
        return DataStoreViewmodelFactory(repository)
    }
}