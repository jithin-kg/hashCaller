package com.hashcaller.app.view.ui.call.utils

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER

@Keep
data class CallersInfoResponseItem (
    @SerializedName("phoneNumber")
    var hash:String = "",
    @SerializedName("firstName")
    val firstName: String? = "",
    @SerializedName("lastName")
    val lastName: String? = "",
    val nameInPhoneBook:String?="",
    var type: Int? = 0,
    var location: String ?= "",
    var carrier: String? = "",
    var country:String? = "",
    var spamCount:Long? = 0L,
    var isRegistered:Boolean? = false,
    @SerializedName("isInfoFoundInDb")
    val isInfoFoundInDb:Int? = INFO_NOT_FOUND_IN_SERVER,
    var imageThumbnail:String? = "",
    val hUid:String? = "",
    val isVerifiedUser:Boolean? = false,
    val bio:String? = "",
    val email:String? = "",
    val avatarGoogle:String? = "",

    ){
}