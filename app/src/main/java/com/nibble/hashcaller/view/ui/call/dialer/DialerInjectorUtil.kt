package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object DialerInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?, lifecycleScope: LifecycleCoroutineScope):DialerViewModelFactory{

        val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }


        val repository = context?.let {
            DialerRepository(
                it,callLogDAO
            )
        }

        val contactSearchRepository = context?.let { ContactSearchRepository(it) }

        return DialerViewModelFactory(repository,contactSearchRepository)
    }

}