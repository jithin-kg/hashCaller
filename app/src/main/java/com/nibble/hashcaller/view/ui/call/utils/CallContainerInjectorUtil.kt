package com.nibble.hashcaller.view.ui.call.utils

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository


/**
 * Created by Jithin KG on 29,July,2020
 */
object CallContainerInjectorUtil {
    fun provideViewModelFactory(context: Context?): CallContainerViewModelFactory {



        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }
        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val blockListPatternRepository = BlockListPatternRepository(blockListDao!!, mutedCallersDAO!!)

        val repository = context?.let {
            CallContainerRepository(
                it,
                callerInfoFromServerDAO!!,
                mutedCallersDAO,
                callLogDAO
            )
        }


        val callLogLiveData =
            CallLogLiveData(context!!, repository)

        return CallContainerViewModelFactory(callLogLiveData!!, repository,callerInfoFromServerDAO, blockListPatternRepository)
    }

}