package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListSpamInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):SMSListSpamViewModelFactory{

        val smsSpamLiveData = context?.let {
            SMSSpamLiveData(
                it
            )
        }
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }

        val repository = context?.let { SMSLocalRepository(it, spamListDAO) }

        return SMSListSpamViewModelFactory(smsSpamLiveData, repository)
    }

}