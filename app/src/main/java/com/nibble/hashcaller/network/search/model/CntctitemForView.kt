package com.nibble.hashcaller.network.search.model


import androidx.annotation.Keep
import com.nibble.hashcaller.network.StatusCodes.Companion.STATUS_SEARHING_IN_PROGRESS

@Keep
data class CntctitemForView(
    val firstName: String = "",

    val lastName: String = "",

    val carrier: String ="",

    val location: String ="",

    val lineType: String ="",

    val country: String="",

    val spammCount : Long = 0L,

    val statusCode:Int = STATUS_SEARHING_IN_PROGRESS
)