package com.hashcaller.app.network.contact

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/**
 * Created by Jithin KG on 25,July,2020
 */
@Keep
data class NetWorkResponse(
//   val status:String,
   @SerializedName("message")
    var message:String=""
) {
}

