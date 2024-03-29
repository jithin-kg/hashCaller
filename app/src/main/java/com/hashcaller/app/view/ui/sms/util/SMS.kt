package com.hashcaller.app.view.ui.sms.util

import android.text.SpannableStringBuilder
import androidx.annotation.Keep
import com.google.gson.Gson


const val SENDER_INFO_SEARCHING = 0
const val SENDER_INFO_FROM_DB= 1
const val SENDER_INFO_FROM_CONTENT_PROVIDER = 2
const val SENDER_INFO_NOT_FOUND = 3
@Keep
data class SMS(var isDummy:Boolean = false,
               var addresStringNonFormated: String = "",
               var sub:String = "",
               var subject:String = "",
               var ct_t:String = "",
               var read_status:String = "",
               var reply_path_present:String = "",
               var body:String = "",
               var msg_box:String = "",
               var thread_id:String = "",
               var sub_cs:String = "",
               var resp_st:String = "",
               var retr_st:String = "",
               var text_only:String = "",
               var locked:String = "",
               var spammerType:Int  = 0,
               var spamCount: Long = 0L,
               var photoURI: String? = null,
               var deleteViewPresent:Boolean = false,
               var firstName:String? = null,
               var lastName:String? = null,
               var firstNameFromServer:String? = null,
               var lastNameFromServer:String? = null,
               var nameForDisplay:String = "",
               var expanded:Boolean = true,
               var threadID: Long = -1L,
               var isSpam: Boolean = false,
               var id: Long = 0,
               var address: SpannableStringBuilder? = null,
               var type:Int = 0,
               var addressString: String? = null,
               var msg: SpannableStringBuilder? = null,
               var searchIndex:Int = 0,
               var msgString:String? = null,
               var msgType:Int = 0,
               var currentDate:String? = null,
               var unReadSMSCount:Int = 0,
               var readState : Int = 0, //"0" for have not read sms and "1" for have read sms
               var time: Long?= 0L,
               var timeString:String = "",
               var folderName: String? = null,
               var color:Int = 0,
               var relativeTime:String = "",
               var senderInfoFoundFrom:Int = SENDER_INFO_SEARCHING,
               var isMarked : Boolean = false,
               var kaatam : String ? = null,
               var spanStartPos:Int = 0, // for address
               var spanEndPos:Int = 0,
               var spanStartPosNameCp:Int = 0, // name from content provider start position
               var spanEndPosNameCp:Int = 0,
               var spanStartPosMsgPeek:Int = 0,
               var spanEndPosMsgPeek:Int = 0,
               ){
    /**
    //     * THIS IS FOR DEEP CLONING ELSE DIFFUTIL NOT RECOGNIZING CHANGES SOME TIMES
    //     */
    fun deepCopy() : SMS {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }
}
//class SMS() {
//    var isDummy = false
//    var addresStringNonFormated: String = ""
//    var sub:String = ""
//    var subject:String = ""
//    var ct_t:String = ""
//    var read_status:String = ""
//    var reply_path_present:String = ""
//    var body:String = ""
//    var msg_box:String = ""
//    var thread_id:String = ""
//    var sub_cs:String = ""
//    var resp_st:String = ""
//    var retr_st:String = ""
//    var text_only:String = ""
//    var locked:String = ""
//
//    //------------------------//
//    var spammerType  = 0
//    var spamCount: Long = 0L
//    var photoURI: String? = null
//    var deleteViewPresent:Boolean = false
//    var name:String? = null
//    var nameForDisplay:String = ""
//    var expanded:Boolean = true
//    var threadID: Long = -1L
//    var isSpam: Boolean = false
//    var id: Long = 0
//    var address: SpannableStringBuilder? = null
//    var type:Int = 0
//    var addressString: String? = null
//    var msg: SpannableStringBuilder? = null
//    var searchIndex = 0
//    var msgString:String? = null
//    var msgType:Int = 0
//    var currentDate:String? = null
//    var unReadSMSCount:Int = 0
//    var readState : Int = 0 //"0" for have not read sms and "1" for have read sms
//    var time: Long?= 0L;
//    var timeString = ""
//    var folderName: String? = null
//    var color = 0
//    var relativeTime:String = ""
//    var senderInfoFoundFrom = SENDER_INFO_SEARCHING // to indicate from
//    // where additional info about a number got from , ie either localdb,
//            // contentprovider or still searching
//
//    override fun equals(obj: Any?): Boolean {
//        val sms = obj as SMS?
//        return address == sms!!.address
//    }
//
//    override fun hashCode(): Int {
//        return address.hashCode()
//    }
//
//    /**
//     * THIS IS FOR DEEP CLONING ELSE DIFFUTIL NOT RECOGNIZING CHANGES SOME TIMES
//     */
//    fun deepCopy() : SMS {
//        return Gson().fromJson(Gson().toJson(this), this.javaClass)
//    }
//
//}