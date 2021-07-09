package com.nibble.hashcaller.network.user

import com.nibble.hashcaller.network.user.Contact

data class Data(
    val firstName: String,
    val image: String?,
    val lastName: String,
    val contacts: List<Contact>,

)