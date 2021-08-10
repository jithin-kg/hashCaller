package com.hashcaller.view.ui.sms.spam

import android.content.Context
import com.hashcaller.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object SpamSMSInjectorUtil {
    fun provideViewmodelFactory(context: Context):SpamSMSViewmodelFactory{
        val threadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val repository = context?.let {
            SpamSMSRepository( threadsDAO, context) }

        return SpamSMSViewmodelFactory( repository)
    }

}