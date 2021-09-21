package com.hashcaller.app.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GoogleProfile(
    var firstName: String="",
    var lastName: String="",
    var email:String="",
)
