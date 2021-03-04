package com.nibble.hashcaller.view.ui.sms.util

import android.text.SpannableStringBuilder
import com.google.gson.Gson

class SMS() {
    var sub:String = ""
    var subject:String = ""
    var ct_t:String = ""
    var read_status:String = ""
    var reply_path_present:String = ""
    var body:String = ""
    var msg_box:String = ""
    var thread_id:String = ""
    var sub_cs:String = ""
    var resp_st:String = ""
    var retr_st:String = ""
    var text_only:String = ""
    var locked:String = ""

    //------------------------//
    var spammerType  = 0
    var spamCount: Long = 0L
    var photoURI: String? = null
    var deleteViewPresent:Boolean = false
    var name:String? = null
    var expanded:Boolean = true
    var threadID: Long = -1L
    var isSpam: Boolean = false
    var id: Long = 0
    var address: SpannableStringBuilder? = null
    var type:Int = 0
    var addressString: String? = null
    var msg: SpannableStringBuilder? = null
    var msgString:String? = null
    var msgType:Int = 0
    var currentDate:String? = null
    var unReadSMSCount:Int = 0
    var readState //"0" for have not read sms and "1" for have read sms
            : Int = 0
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

    /**
     * THIS IS FOR DEEP CLONING ELSE DIFFUTIL NOT RECOGNIZING CHANGES SOME TIMES
     */
    fun deepCopy() : SMS {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }

}