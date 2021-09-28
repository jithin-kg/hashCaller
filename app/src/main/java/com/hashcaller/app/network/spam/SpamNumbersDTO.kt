package com.hashcaller.app.network.spam

import androidx.annotation.Keep

@Keep
data class SpamNumbersDTO(
    var phoneNumbers: List<String>,
    var country: String = "",
    var spammerType: String = ""
) {
}