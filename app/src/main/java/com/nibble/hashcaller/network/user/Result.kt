package com.nibble.hashcaller.network.user

import androidx.annotation.Keep

@Keep
data class Result(
//    val email: String,
    val firstName: String,
//    val gender: String,
    val lastName: String,
    val image: String?,
    val customToken:String?,
    val isBlockedByAdmin:Int = 0

)