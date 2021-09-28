package com.hashcaller.app.network.spam

import androidx.annotation.Keep

@Keep
data class SpamThresholdResponse(
    val threshold: Int

    ) {
}