package com.hashcaller.app.network.search.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SpammerStatus(
    @SerializedName("spamCount")
    val spamCount:Int,
    @SerializedName("spammer")
    val spammer:Boolean
    ): Serializable