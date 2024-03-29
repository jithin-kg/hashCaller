package com.hashcaller.app.view.ui.sms.identifiedspam

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.sms.util.SMSLocalRepository
import com.hashcaller.app.view.ui.sms.util.SmsRepositoryHelper
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

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
            callLogDAO,
            SmsRepositoryHelper(context),
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        ) }

        return SMSListSpamViewModelFactory(smsSpamLiveData, repository)
    }

}