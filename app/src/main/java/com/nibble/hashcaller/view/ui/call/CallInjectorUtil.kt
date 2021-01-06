package com.nibble.hashcaller.view.ui.call

import android.content.Context
import com.nibble.hashcaller.view.ui.call.dialer.DialerViewModelFactory
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData

object CallInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?): CallviewModelFactory {

        val callLogLiveData = context?.let { CallLogLiveData(it) }

        return CallviewModelFactory(callLogLiveData)
    }

}