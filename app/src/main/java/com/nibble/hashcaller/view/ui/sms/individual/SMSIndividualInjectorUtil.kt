package com.nibble.hashcaller.view.ui.sms.individual

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository


/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSIndividualInjectorUtil {
    fun provideViewModelFactory(context: Context?):SMSIndividualViewModelFactory{

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val smsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsDAO() }
        val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }

        val repository = context?.let { SMSLocalRepository(it, spamListDAO, smssendersInfoDAO) }
        val spamNetworkRepository = context?.let { SpamNetworkRepository(it, spamListDAO) }


        val messagesLiveData = context?.let {
            SMSIndividualLiveData(
                it, IndividualSMSActivity.contact,
                spamListDAO
            )
        }
        return SMSIndividualViewModelFactory(messagesLiveData!!, repository,smsDAO, spamNetworkRepository)
    }

}