package com.hashcaller.app.repository.contacts

import androidx.annotation.Keep

@Keep
data class ContactsSyncDTO(
    val contacts: List<ContactUploadDTO>,
    val countryCode: String,
    val countryISO: String
) {
}