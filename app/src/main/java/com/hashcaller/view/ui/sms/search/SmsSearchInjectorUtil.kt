package com.hashcaller.view.ui.sms.search

import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.utils.notifications.tokeDataStore
import com.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.view.utils.LibPhoneCodeHelper

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
            SmsRepositoryHelper(context),
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        ) }
        val smsSearchRepository =
            SMSSearchRepository(smsSearchQueriesDAO!!)
        return SmsSearchViewModelFactory( repository, smsSearchRepository)
    }

}