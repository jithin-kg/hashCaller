package com.hashcaller.app.network.user

import androidx.annotation.Keep

@Keep
data class GetUserDataResponse(
    val data: Data,
    val statusCode: Int
)