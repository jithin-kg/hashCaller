package com.hashcaller.app.view.ui.call.individualCallLog

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import com.hashcaller.app.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object IndividualCallLogInjectorUtil {
    fun provideDialerViewModelFactory(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        Uri: Uri
    ):IndividualCallLogViewModelFactory{
        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val livedata = context?.let { IndividualCallLivedata(it, lifecycleScope, Uri) }


        val repository = context?.let {
            IndividualCallhistoryRepository(callerInfoFromServerDAO, context  ) }

        return IndividualCallLogViewModelFactory( repository, livedata)
    }

}