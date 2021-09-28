package com.hashcaller.app.repository.contacts

import androidx.annotation.Keep

@Keep
data class ContactUploadDTO(
    var name:String="",
    var hashedPhoneNumber:String = "",
    ){

}
