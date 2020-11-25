package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData

/**
 * Created by Jithin KG on 29,July,2020
 */
object DialerInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):DialerViewModelFactory{

        val callLogLiveData = context?.let { CallLogLiveData(it) }

        return DialerViewModelFactory(callLogLiveData)
    }

}