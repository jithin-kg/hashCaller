package com.nibble.hashcaller.view.ui.sms.search

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SmsSearchInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):SmsSearchViewModelFactory{

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val smsSearchQueriesDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSearchQueriesDAO() }

        val repository = context?.let { SMSLocalRepository(it, spamListDAO, smssendersInfoDAO) }
        val smsSearchRepository =
            SMSSearchRepository(smsSearchQueriesDAO!!)
        return SmsSearchViewModelFactory( repository, smsSearchRepository)
    }

}