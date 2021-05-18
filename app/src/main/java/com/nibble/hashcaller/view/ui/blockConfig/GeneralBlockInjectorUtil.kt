package com.nibble.hashcaller.view.ui.blockConfig

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO

object GeneralBlockInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : GeneralBlockViewModelFactory {

        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val blockListPatternRepository = BlockListPatternRepository(blockListDao, mutedCallersDao)
        val callLogDAO = HashCallerDatabase.getDatabaseInstance(context).callLogDAO()
         val smsThreadsDAO: ISMSThreadsDAO = HashCallerDatabase.getDatabaseInstance(context).smsThreadsDAO()


        val generalBlockRepository = GeneralBlockRepository(callLogDAO, smsThreadsDAO)
            return GeneralBlockViewModelFactory(blockListPatternRepository, generalBlockRepository  )
    }
}