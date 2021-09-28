package com.hashcaller.app.network.user

import androidx.annotation.Keep

@Keep
data class Data(
    val firstName: String,
    val image: String?,
    val lastName: String,
    val contacts: List<Contact>,

)