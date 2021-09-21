package com.hashcaller.app.network.user

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ResUpdateProfileWithGoogle(
    val firstName: String,
    val lastName: String,
    val email:String,
    val bio:String,
    val avatarGoogle: String?,
    val googleProfile: GoogleProfile
)
