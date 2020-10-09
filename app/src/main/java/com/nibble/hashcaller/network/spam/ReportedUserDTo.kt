package com.nibble.hashcaller.network.spam

import androidx.annotation.Keep

@Keep
data class ReportedUserDTo( var phoneNumber:String = "", var location:String = "") {
}