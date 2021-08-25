package com.hashcaller.app.network.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class SearchResponse (
//   val status:String,
    @SerializedName("message")
    var message:String="default",
    var name:String="",
    var phoneNum:String=""
) {
}