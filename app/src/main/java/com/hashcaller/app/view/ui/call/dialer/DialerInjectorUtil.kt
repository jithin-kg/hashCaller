package com.hashcaller.app.view.ui.call.dialer

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.notifications.tokeDataStore

/**
 * Created by Jithin KG on 29,July,2020
 */
object DialerInjectorUtil {
    fun provideDialerViewModelFactory(context: Context, lifecycleScope: LifecycleCoroutineScope):DialerViewModelFactory{

        val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }
        var spamThreshold = DEFAULT_SPAM_THRESHOLD
        lifecycleScope.launchWhenCreated {
             spamThreshold = DataStoreRepository(context.tokeDataStore).getInt(
                PreferencesKeys.SPAM_THRESHOLD)?: DEFAULT_SPAM_THRESHOLD
        }

        val repository = context?.let {
            DialerRepository(
                it,callLogDAO,
                spamThreshold
            )
        }

        val contactSearchRepository = context?.let { ContactSearchRepository(it) }

        return DialerViewModelFactory(repository,contactSearchRepository)
    }

}