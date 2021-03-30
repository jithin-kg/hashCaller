package com.nibble.hashcaller.view.ui.call.dialer.util

import androidx.annotation.Keep

@Keep
class CallLogData(
    var id: Long,
    var number: String = "",
    var type: Int,
    var duration: String,
    var name: String? = "",
    var date: String,
    var expanded:Boolean = false,
    var dateInMilliseconds:String = "",
    var spamCount:Long = 0,
    var relativeTime:String = ""

)  {




}