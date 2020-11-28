package com.nibble.hashcaller.view.ui.smsview.util

import android.text.SpannableStringBuilder

class SMS {
    var id: Long = 0
    var address: SpannableStringBuilder? = null
    var addressString: String? = null
    var msg: SpannableStringBuilder? = null
    var msgString:String? = null
    var readState //"0" for have not read sms and "1" for have read sms
            : String? = null
    var time: Long?= 0L;
    var folderName: String? = null
    var color = 0

    override fun equals(obj: Any?): Boolean {
        val sms = obj as SMS?
        return address == sms!!.address
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

}