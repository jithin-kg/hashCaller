package com.hashcaller.app.view.ui.auth.getinitialInfos

import androidx.annotation.Keep

@Keep
data class GoogleUserProfile(
    val firstName:String,
    val lastName:String,
    val photoUrI:String,
    val email:String
) {
}