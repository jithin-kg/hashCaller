package com.nibble.hashcaller.network.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContactsUploadResponse(
    @SerializedName("cntcts")
    val cntcts:List<ContactUploadResponseItem>,
    @SerializedName("message")
    val message: String
):Serializable {
}