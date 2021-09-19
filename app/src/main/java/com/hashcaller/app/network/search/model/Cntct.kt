package com.hashcaller.app.network.search.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import java.io.Serializable

@Keep
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
    val thumbnailImg:String = "",

    @SerializedName("nameInPhoneBook")
    val nameInPhoneBook:String = "",

    @SerializedName("hUid")
    val hUid:String = "",

    @SerializedName("email")
    val email:String = "",

    @SerializedName("avatarGoogle")
    val avatarGoogle:String = "",

    @SerializedName("bio")
    val bio:String = "",


):Serializable // it it important to extent serializable