package com.hashcaller.app.repository.user

import androidx.annotation.Keep

@Keep
data class UserInfoDTO(

    var firstName:String="",
    var lastName:String="",
    var hashedNum:String = "",
    var phoneNumber:String = "",
    var countryCode: String = "",
    var countryISO: String = "",
    var googleProfileImgUrl:String = "",
    var bio:String = "",
    var email:String = ""
//    var phoneNumber:String = "912",
//    var email:String = "",
//    var gender:String = "32",
//    @Part("profilePic")
//    var profilePic : MultipartBody.Part? = null
)