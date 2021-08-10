package com.hashcaller.network.user

import androidx.annotation.Keep

@Keep
data class GetUserInfoDTO(
    val hashedNum:String,
    val formattedPhoneNum:String
) {

}