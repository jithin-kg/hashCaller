package com.nibble.hashcaller.repository.user

import androidx.annotation.Keep
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
@Keep
data class UserInfoDTO(

    var firstName:String="",
    var lastName:String="",
    var phoneNumber:String = "912",
    var email:String = "",
    var gender:String = "32",
    @Part("profilePic")
    var profilePic : MultipartBody.Part? = null
)