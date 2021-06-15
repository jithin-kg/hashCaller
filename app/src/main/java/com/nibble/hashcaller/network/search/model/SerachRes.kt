package com.nibble.hashcaller.network.search.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class SerachRes (
    @SerializedName("cntcts")
    val cntcts: Cntct?,
    @SerializedName("status")
    val status: Int
):Serializable