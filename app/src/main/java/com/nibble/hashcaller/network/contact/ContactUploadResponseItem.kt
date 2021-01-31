package com.nibble.hashcaller.network.contact

import com.google.gson.annotations.SerializedName
import com.nibble.hashcaller.network.search.model.SpammerStatus
import java.io.Serializable

data class ContactUploadResponseItem(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("carrier")
    val carrier : String,

    @SerializedName("location")
    val location: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("lineType")
    val lineType:String,

    @SerializedName("spamCount")
    val spamCount:Int,

    @SerializedName("name")
    val name:String

) : Serializable{
}