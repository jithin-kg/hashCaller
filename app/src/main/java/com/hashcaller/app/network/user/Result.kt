package com.hashcaller.app.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Result(
//    val email: String,
    var firstName: String="",
//    val gender: String,
    var lastName: String="",
    var image: String?,
    @SerializedName("customToken")
    var customToken:String?,
    var isBlockedByAdmin:Int = 0,
    var isPhoneNumRemovedInFireBs:Boolean = false,
    var bio:String="",
    var email:String="",
    var avatarGoogle:String="",
    var isVerifiedUser:Boolean = false,

)