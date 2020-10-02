package com.nibble.hashcaller.network.search.model


import com.google.gson.annotations.SerializedName

data class Cntct(
    @SerializedName("carrier")
    val carrier: String,
    @SerializedName("coutry")
    val coutry: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String
)