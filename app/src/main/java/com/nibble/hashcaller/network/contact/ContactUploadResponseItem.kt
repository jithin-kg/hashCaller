package com.nibble.hashcaller.network.contact

import com.google.gson.annotations.SerializedName
import com.nibble.hashcaller.network.search.model.SpammerStatus
import java.io.Serializable

data class ContactUploadResponseItem(
    @SerializedName("firstNDigits")
    val firstNDigits: String,
    @SerializedName("carrier")
    val carrier : String,

    @SerializedName("location")
    val location: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("lineType")
    val lineType:String,

    @SerializedName("spamCount:number")
    val spamCount:Int

) : Serializable{
}