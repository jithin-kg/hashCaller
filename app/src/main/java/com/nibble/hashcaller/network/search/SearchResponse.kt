package com.nibble.hashcaller.network.search

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class SearchResponse (
//   val status:String,
    @SerializedName("message")
    var message:String="default",
    var name:String="default"
) {
}