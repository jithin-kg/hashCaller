package com.hashcaller.app.view.ui.call.spam

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.hashcaller.app.local.db.HashCallerDatabase

/**
 * Created by Jithin KG on 29,July,2020
 */
object SpamCallInjectorUtil {
    fun provideViewmodelFactory(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        spamThreshold: Int
    ):SpamCallViewmodelFactory{
        val callLogDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }

        val repository = context?.let {
            SpamCallRepository( callLogDao, context, spamThreshold) }

        return SpamCallViewmodelFactory( repository)
    }

}