package com.android.internal.telephony

import androidx.annotation.Keep

/**
 * Created by Jithin KG on 20,July,2020
 */
@Keep
interface ITelephony   {
    fun endCall(): Boolean
    fun answerRingingCall()
    fun silenceRinger()
}
