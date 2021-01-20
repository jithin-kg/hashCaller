package com.nibble.hashcaller.view.ui.sms.work

data class Contact(
    val oldHash: String,
    val newHash: String,
    val name: String,
    val spamCount: Long
)