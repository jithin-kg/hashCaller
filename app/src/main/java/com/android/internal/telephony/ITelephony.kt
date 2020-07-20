package com.android.internal.telephony

/**
 * Created by Jithin KG on 20,July,2020
 */
interface ITelephony {
    fun endCall(): Boolean
    fun answerRingingCall()
    fun silenceRinger()
}
