package com.hashcaller.app.network.user

import androidx.annotation.Keep

@Keep
data class Contact(
    val hashedPhoneNumber: String,
    val name: String
)