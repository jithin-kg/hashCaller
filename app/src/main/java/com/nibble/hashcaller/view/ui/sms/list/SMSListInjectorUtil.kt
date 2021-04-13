package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):SMSListViewModelFactory{


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
        val messagesLiveData = context?.let {
            SMSLiveData(
                it,repository
            )
        }

        return SMSListViewModelFactory(messagesLiveData, repository)
    }

}