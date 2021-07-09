package com.nibble.hashcaller.network.search.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class SerachRes (
    @SerializedName("data")
    val cntcts: Cntct?,
    @SerializedName("message")
    val message:String
):Serializable