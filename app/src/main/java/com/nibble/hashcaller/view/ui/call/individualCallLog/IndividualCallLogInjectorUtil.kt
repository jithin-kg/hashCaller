package com.nibble.hashcaller.view.ui.call.individualCallLog

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object IndividualCallLogInjectorUtil {
    fun provideDialerViewModelFactory(context: Context):IndividualCallLogViewModelFactory{
        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val livedata = context?.let { IndividualCallLivedata(it) }


        val repository = context?.let {
            IndividualCallhistoryRepository(callerInfoFromServerDAO, context  ) }

        return IndividualCallLogViewModelFactory( repository, livedata)
    }

}