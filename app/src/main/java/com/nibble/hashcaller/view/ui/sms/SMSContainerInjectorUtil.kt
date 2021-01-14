package com.nibble.hashcaller.view.ui.sms

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData


/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSContainerInjectorUtil {
    fun provideViewModelFactory(context: Context?):SMSCotainerViewModelFactory{


        val spammerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spammerInfoFromServerDAO() }

        val repository = context?.let { SMScontainerRepository(it, spammerInfoFromServerDAO!!) }


        val messagesLiveData =
            SMSLiveData(context!!)

        return SMSCotainerViewModelFactory(messagesLiveData!!, repository,spammerInfoFromServerDAO)
    }

}