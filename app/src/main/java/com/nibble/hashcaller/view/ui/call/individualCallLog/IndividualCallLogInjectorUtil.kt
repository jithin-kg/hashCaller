package com.nibble.hashcaller.view.ui.call.individualCallLog

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object IndividualCallLogInjectorUtil {
    fun provideDialerViewModelFactory(context: Context, lifecycleScope: LifecycleCoroutineScope):IndividualCallLogViewModelFactory{
        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val livedata = context?.let { IndividualCallLivedata(it, lifecycleScope) }


        val repository = context?.let {
            IndividualCallhistoryRepository(callerInfoFromServerDAO, context  ) }

        return IndividualCallLogViewModelFactory( repository, livedata)
    }

}