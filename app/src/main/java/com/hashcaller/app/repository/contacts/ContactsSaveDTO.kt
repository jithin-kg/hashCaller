package com.hashcaller.app.repository.contacts

import androidx.annotation.Keep

@Keep
data class ContactsSaveDTO(
    val contacts: List<ContactUploadDTO>,

) {
}