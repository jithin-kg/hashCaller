package com.hashcaller.app.network.incomingcall

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SuggestNameModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("number")
    val number: String
) {
    data class Response(
        @SerializedName("data")
        val `data`: String,
        @SerializedName("message")
        val message: String,
        @SerializedName("statusCode")
        val statusCode: Int
    )
}