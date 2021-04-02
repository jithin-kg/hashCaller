package com.nibble.hashcaller.view.ui.call.dialer.util

import androidx.annotation.Keep
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING

@Keep
class CallLogData(
    var id: Long? = null,
    var number: String = "",
    var type: Int= 0,
    var duration: String ="",
    var name: String? = "",
    var date: String = "",
    var expanded:Boolean = false,
    var dateInMilliseconds:String = "",
    var spamCount:Long = 0,
    var relativeTime:String = "",
    var callerInfoFoundFrom: Int = SENDER_INFO_SEARCHING,
    var color:Int = 1

)  {




}