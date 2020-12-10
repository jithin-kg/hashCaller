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

        val messagesLiveData = context?.let {
            SMSIndividualLiveData(
                it, IndividualSMSActivity.contact
            )
        }
        val smsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsDAO() }

        val repository = context?.let { SMSLocalRepository(it) }
        val spamNetworkRepository = context?.let { SpamNetworkRepository(it) }

        return SMSIndividualViewModelFactory(messagesLiveData!!, repository,smsDAO, spamNetworkRepository)
    }

}