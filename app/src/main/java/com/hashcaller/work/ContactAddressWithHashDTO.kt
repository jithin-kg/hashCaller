package com.hashcaller.work

import androidx.annotation.Keep

/**
 * In server api we need original phone number to identify country.
 */
@Keep
data class ContactAddressWithHashDTO(
    val contactAddressHashed:String
) {
}