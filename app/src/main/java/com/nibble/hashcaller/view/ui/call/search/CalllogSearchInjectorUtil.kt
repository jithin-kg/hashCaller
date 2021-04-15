package com.nibble.hashcaller.view.ui.call.search

import android.content.Context
import android.provider.CallLog
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.call.repository.CallLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object CalllogSearchInjectorUtil {
    fun provideDialerViewModelFactory(context: Context):CallLogSearchViewModelFactory{
        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }

        val projection = arrayOf(
            CallLog.Calls.NUMBER  ,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls._ID,
            CallLog.Calls.DATE

        )

        val repository = context?.let {
            CallLogSearchRepository(callerInfoFromServerDAO, callLogDAO  ) }

        return CallLogSearchViewModelFactory( repository)
    }

}