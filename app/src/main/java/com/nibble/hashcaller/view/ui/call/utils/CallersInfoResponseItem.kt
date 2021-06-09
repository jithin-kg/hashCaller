package com.nibble.hashcaller.view.ui.call.utils

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.nibble.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER

@Keep
data class CallersInfoResponseItem (
    var phoneNumber:String = "",
    var type: Int = 0,
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
    var location: String = "",
    var country:String = "",
    var carrier: String = "",
    var spamCount:Long = 0L,
    @SerializedName("isInfoFoundInDb")
    val isInfoFoundInDb:Int = INFO_NOT_FOUND_IN_SERVER,
    var imageThumbnail:String? = ""
){
}