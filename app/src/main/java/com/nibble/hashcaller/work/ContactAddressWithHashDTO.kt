package com.nibble.hashcaller.work

import androidx.annotation.Keep

@Keep
data class ContactAddressWithHashDTO(
    val contactAddersString:String,
    val contactAddressHashed:String
) {
}