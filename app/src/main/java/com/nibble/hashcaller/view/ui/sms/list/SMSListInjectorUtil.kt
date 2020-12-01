package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import com.nibble.hashcaller.view.ui.sms.util.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object SMSListInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):SMSListViewModelFactory{

        val messagesLiveData = context?.let {
            SMSLiveData(
                it
            )
        }
        val repository = context?.let { SMSLocalRepository(it) }

        return SMSListViewModelFactory(messagesLiveData, repository)
    }

}