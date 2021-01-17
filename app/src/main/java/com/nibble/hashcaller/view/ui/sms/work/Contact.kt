package com.nibble.hashcaller.view.ui.sms.work

data class Contact(
    val hashOne: String,
    val hashTwo: String,
    val name: String,
    val spammerStatus: SpammerStatus
)