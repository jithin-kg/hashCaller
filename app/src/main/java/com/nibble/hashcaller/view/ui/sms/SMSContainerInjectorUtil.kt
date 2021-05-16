package com.nibble.hashcaller.view.ui.sms

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData2


/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSContainerInjectorUtil {
    fun provideViewModelFactory(context: Context?, scope:LifecycleCoroutineScope): SMSCotainerViewModelFactory? {


        val spammerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
        val blockedOrSpamSenders = context?.let { HashCallerDatabase.getDatabaseInstance(it).blockedOrSpamSendersDAO() }
//        val repository = context?.let { SMScontainerRepository(
//            it, spammerInfoFromServerDAO!!, mutedSendersDAO, blockedOrSpamSenders,
//            DataStoreRepository(it.tokeDataStore),
//            tokenHelper
//        ) }


        val messagesLiveData =
            SMSLiveData2(context!!, scope)
//        return SMSCotainerViewModelFactory(messagesLiveData!!, repository,spammerInfoFromServerDAO, mutedSendersDAO)
        return null
    }

}