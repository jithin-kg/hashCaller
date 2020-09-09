package com.nibble.hashcaller.repository.user

import androidx.annotation.Keep

@Keep
data class UserInfoDTO(
    var firstName:String="",
    var lastName:String="",
    var phoneNumber:String = "",
    var email:String = "",
    var gender:String = ""
)