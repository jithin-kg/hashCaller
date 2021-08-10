package com.hashcaller.network.user

data class Data(
    val firstName: String,
    val image: String?,
    val lastName: String,
    val contacts: List<Contact>,

)