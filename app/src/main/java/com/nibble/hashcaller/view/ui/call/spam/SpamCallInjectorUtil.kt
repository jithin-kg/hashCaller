package com.nibble.hashcaller.view.ui.call.spam

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object SpamCallInjectorUtil {
    fun provideViewmodelFactory(context: Context):SpamCallViewmodelFactory{
        val callLogDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }

        val repository = context?.let {
            SpamCallRepository( callLogDao) }

        return SpamCallViewmodelFactory( repository)
    }

}