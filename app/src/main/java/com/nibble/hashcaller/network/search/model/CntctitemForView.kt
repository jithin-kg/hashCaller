package com.nibble.hashcaller.network.search.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class CntctitemForView(
    val firstName: String? = "",

    val lastName: String? = "",

    val carrier: String?="",

    val location: String?="",

    val lineType: String,

    val country: String?="",

    val spammCount : Int? = 0,

    val statusCode:Int = 0
)