package com.hashcaller.app.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateProfileResult(
    val firstName: String,
    val lastName: String,
    val email:String,
    val bio:String,
    val image: String?
)