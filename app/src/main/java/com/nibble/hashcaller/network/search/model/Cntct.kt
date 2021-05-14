package com.nibble.hashcaller.network.search.model


import com.google.gson.annotations.SerializedName
import com.nibble.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import java.io.Serializable

data class Cntct(

    @SerializedName("firstName")
    val firstName: String? = "",

    @SerializedName("lastName")
    val lastName: String? = "",

    @SerializedName("carrier")
    val carrier: String?="",

    @SerializedName("location")
    val location: String?="",

    @SerializedName("lineType")
    val lineType: String,

    @SerializedName("country")
    val country: String?="",

    @SerializedName("spamCount")
    val spammCount : Long? = 0,

    @SerializedName("isInfoFoundInDb")
    val isInfoFoundInDb:Int = INFO_NOT_FOUND_IN_SERVER,

    @SerializedName("thumbnailImg")
    val thumbnailImg:String = ""





):Serializable // it it important to extent serializable