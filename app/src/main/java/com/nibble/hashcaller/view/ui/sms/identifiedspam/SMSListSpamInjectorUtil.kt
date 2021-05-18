package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListSpamInjectorUtil {
    fun provideDialerViewModelFactory(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        tokenHelper: TokenHelper?
    ):SMSListSpamViewModelFactory{

        val smsSpamLiveData = context?.let {
            SMSSpamLiveData(
                it,lifecycleScope
            )
        }
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
        val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val callLogDAO = context?.let{HashCallerDatabase.getDatabaseInstance(it).callLogDAO()}
        val repository = context?.let { SMSLocalRepository(
            it,
            spamListDAO,
            callerInfoFromServerDAO,
            mutedSendersDAO,
            smsThreadsDAO,
            DataStoreRepository(context.tokeDataStore),
            tokenHelper,
            callLogDAO
        ) }

        return SMSListSpamViewModelFactory(smsSpamLiveData, repository)
    }

}