package com.nibble.hashcaller.view.ui.sms.search

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.getAllSMSCursor
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper

/**
 * Created by Jithin KG on 29,July,2020
 */
object SmsSearchInjectorUtil {

    fun provideDialerViewModelFactory(context: Context?, tokenHelper: TokenHelper?):SmsSearchViewModelFactory{

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val smsSearchQueriesDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSearchQueriesDAO() }
        val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
        val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }
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
        val smsSearchRepository =
            SMSSearchRepository(smsSearchQueriesDAO!!)
        return SmsSearchViewModelFactory( repository, smsSearchRepository)
    }

}