package com.hashcaller.app.view.ui.sms.work

import androidx.annotation.Keep

@Keep
data class Contact(
    val phoneNumber: String = "",
    val newHash: String = "",
    val name: String = "",
    val spamCount: Long = 0
)