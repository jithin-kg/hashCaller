package com.nibble.hashcaller.view.ui.call.utils

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository


/**
 * Created by Jithin KG on 29,July,2020
 */
object CallContainerInjectorUtil {
    fun provideViewModelFactory(context: Context?): CallContainerViewModelFactory {


        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }

        val repository = context?.let {
            CallContainerRepository(
                it,
                callerInfoFromServerDAO!!,
                mutedCallersDAO
            )
        }


        val callLogLiveData =
            CallLogLiveData(context!!)

        return CallContainerViewModelFactory(callLogLiveData!!, repository,callerInfoFromServerDAO)
    }

}