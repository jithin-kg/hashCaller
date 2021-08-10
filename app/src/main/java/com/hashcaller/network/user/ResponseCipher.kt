package com.hashcaller.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep

data class ResponseCipher(@SerializedName("cipher")var  cipher:String) {
}