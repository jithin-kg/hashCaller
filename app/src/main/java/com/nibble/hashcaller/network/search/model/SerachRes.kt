package com.nibble.hashcaller.network.search.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SerachRes (
    @SerializedName("cntcts")
    val cntcts: Cntct?,
    @SerializedName("status")
    val status: Int
):Serializable