package com.hashcaller.app.network.contact

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class ContactsUploadResponse(
    @SerializedName("cntcts")
    val cntcts:List<ContactUploadResponseItem>,
    @SerializedName("message")
    val message: String
):Serializable {
}