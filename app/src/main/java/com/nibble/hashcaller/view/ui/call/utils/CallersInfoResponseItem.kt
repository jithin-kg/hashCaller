package com.nibble.hashcaller.view.ui.call.utils

import androidx.annotation.Keep

@Keep
data class CallersInfoResponseItem (
    var phoneNumber:String = "",
    var type: Int = 0,
    var name: String = "",
    var city: String = "",
    var country:String = "",
    var carier: String = "",
    var spamCount:Long = 0L
){
}