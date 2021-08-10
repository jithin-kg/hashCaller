package com.hashcaller.network.contact

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
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