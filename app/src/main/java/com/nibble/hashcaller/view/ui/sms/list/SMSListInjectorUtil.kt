package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.blockConfig.GeneralBlockRepository
import com.nibble.hashcaller.view.ui.contacts.getAllSMSCursor
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListInjectorUtil {
    fun provideDialerViewModelFactory(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        tokenHelper: TokenHelper?
    ):SMSListViewModelFactory{


        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
        val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
         val blockListPatternRepository: BlockListPatternRepository = BlockListPatternRepository(blockListDao, mutedCallersDao)
        val callLogDAO = context?.let{HashCallerDatabase.getDatabaseInstance(it).callLogDAO()}

        val repository = context?.let { SMSLocalRepository(
            it,
            spamListDAO,
            smssendersInfoDAO,
            mutedSendersDAO,
            smsThreadsDAO,
            DataStoreRepository(context.tokeDataStore),
            tokenHelper,
            callLogDAO,
            SmsRepositoryHelper(context.getAllSMSCursor())
        ) }
        val messagesLiveData = context?.let {
            SMSLiveData(
                it,repository, lifecycleScope
            )
        }

        val generalBlockRepository = GeneralBlockRepository(callLogDAO, smsThreadsDAO, blockListDao)

        return SMSListViewModelFactory(messagesLiveData, repository, blockListPatternRepository, generalBlockRepository)
    }

}