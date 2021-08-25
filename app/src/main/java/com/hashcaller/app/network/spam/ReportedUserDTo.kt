package com.hashcaller.app.network.spam

import androidx.annotation.Keep

@Keep
data class ReportedUserDTo(
    var phoneNumber: String = "",
    var country: String = "",
    var spammerType: String = "",
) {
}