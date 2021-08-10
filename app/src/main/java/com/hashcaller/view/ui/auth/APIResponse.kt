package com.hashcaller.view.ui.auth

import androidx.annotation.Keep

@Keep
data class APIResponse(
    val success: Boolean,
    val challenge_ts: List<String>,
    val apk_package_name: String

)