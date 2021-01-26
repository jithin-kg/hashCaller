package com.nibble.hashcaller.network.search.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Cntct(
    @SerializedName("name")
    val name: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("spammCount")
    val spammCount : Int,
    @SerializedName("carrier")
    val carrier: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("country")
    val country: String

):Serializable // it it important to extent serializable