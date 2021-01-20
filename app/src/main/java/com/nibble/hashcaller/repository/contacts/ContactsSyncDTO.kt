package com.nibble.hashcaller.repository.contacts

import androidx.annotation.Keep

@Keep
data class ContactsSyncDTO(
    val contacts: List<ContactUploadDTO>,
    val countryCode: String,
    val countryISO: String
) {
}