package com.hashcaller.app.network.spam

data class SpamNumbersDTO(
    var phoneNumbers: List<String>,
    var country: String = "",
    var spammerType: String = ""
) {
}