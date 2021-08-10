package com.hashcaller.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Result(
//    val email: String,
    val firstName: String,
//    val gender: String,
    val lastName: String,
    val image: String?,
    @SerializedName("customToken")
    val customToken:String?,
    val isBlockedByAdmin:Int = 0,
    val isPhoneNumRemovedInFireBs:Boolean = false

)