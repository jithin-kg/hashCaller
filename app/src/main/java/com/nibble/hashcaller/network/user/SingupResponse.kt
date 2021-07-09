package com.nibble.hashcaller.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SingupResponse(
    @SerializedName("data")
    val data: Result,

    @SerializedName("message")
    val message:String
)