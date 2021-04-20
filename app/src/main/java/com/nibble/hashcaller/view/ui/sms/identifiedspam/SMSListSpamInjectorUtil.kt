package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListSpamInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?, lifecycleScope: LifecycleCoroutineScope):SMSListSpamViewModelFactory{

        val smsSpamLiveData = context?.let {
            SMSSpamLiveData(
                it,lifecycleScope
            )
        }
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
        val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val repository = context?.let { SMSLocalRepository(
            it,
            spamListDAO,
            smssendersInfoDAO,
            mutedSendersDAO,
            smsThreadsDAO
        ) }

        return SMSListSpamViewModelFactory(smsSpamLiveData, repository)
    }

}