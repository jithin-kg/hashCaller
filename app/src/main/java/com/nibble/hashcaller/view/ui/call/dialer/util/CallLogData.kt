package com.nibble.hashcaller.view.ui.call.dialer.util

import androidx.annotation.Keep

@Keep
class CallLogData(
    val id: String,
    val number: String = "",
    val type: Int,
    val duration: String,
    var name: String? = "",
    val date: String,
    var expanded:Boolean = false,
    var dateInMilliseconds:String = ""

)  {




}