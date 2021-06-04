package com.nibble.hashcaller.view.ui.sms.individual

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.getAllSMSCursor
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper


/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSIndividualInjectorUtil {
    fun provideViewModelFactory(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        tokenHelper: TokenHelper?
    ):SMSIndividualViewModelFactory{

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
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
            SmsRepositoryHelper(context.getAllSMSCursor()),
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        ) }
        val spamNetworkRepository = context?.let { SpamNetworkRepository(
            it,
            spamListDAO,
            DataStoreRepository(context.tokeDataStore)
        ) }

        val smsLocalRepository = SMSLocalRepository(
            context!!,
            spamListDAO,
            smssendersInfoDAO,
            mutedSendersDAO,
            smsThreadsDAO,
            DataStoreRepository(context.tokeDataStore),
            tokenHelper,
            callLogDAO,
            SmsRepositoryHelper(context.getAllSMSCursor()),
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        )

        val messagesLiveData = context?.let {
            SMSIndividualLiveData(
                it, IndividualSMSActivity.contact,
                spamListDAO,lifecycleScope,
                callLogDAO
            )
        }
        return SMSIndividualViewModelFactory(messagesLiveData!!, repository,smsDAO, spamNetworkRepository, smsLocalRepository)
    }



}