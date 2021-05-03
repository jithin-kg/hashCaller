package com.nibble.hashcaller.datastore

import android.content.Context

object DataStoreInjectorUtil {

    fun providerViewmodelFactory(context: Context) : DataStoreViewmodelFactory{

        val repository = DataStoreRepository(context)
        return DataStoreViewmodelFactory(repository)
    }
}