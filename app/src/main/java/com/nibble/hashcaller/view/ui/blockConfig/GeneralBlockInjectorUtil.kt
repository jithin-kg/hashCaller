package com.nibble.hashcaller.view.ui.blockConfig

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO

object GeneralBlockInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : GeneralBlockViewModelFactory {

        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val smsThreadsDAO: ISMSThreadsDAO = HashCallerDatabase.getDatabaseInstance(context).smsThreadsDAO()
        val callLogDAO = HashCallerDatabase.getDatabaseInstance(context).callLogDAO()

        val blockListPatternRepository = BlockListPatternRepository(blockListDao, mutedCallersDao)

        val generalBlockRepository = GeneralBlockRepository(callLogDAO, smsThreadsDAO, blockListDao)

            return GeneralBlockViewModelFactory(blockListPatternRepository, generalBlockRepository  )
    }
}