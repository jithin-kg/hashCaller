package com.hashcaller.view.ui.call.utils

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UnknownCallersInfoResponse (
    @SerializedName("data")
    val contacts: List<CallersInfoResponseItem>
){

}