package com.nibble.hashcaller.view.ui.call.utils

import androidx.annotation.Keep

@Keep
data class UnknownCallersInfoResponse (
    val contacts: List<CallersInfoResponseItem>
){

}