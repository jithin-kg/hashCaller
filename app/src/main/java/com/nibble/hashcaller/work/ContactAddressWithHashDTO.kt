package com.nibble.hashcaller.work

import androidx.annotation.Keep

/**
 * In server api we need original phone number to identify country.
 */
@Keep
data class ContactAddressWithHashDTO(
    val contactAddressString:String,
    val contactAddressHashed:String
) {
}