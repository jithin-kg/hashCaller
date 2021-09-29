package com.hashcaller.app.utils.updatemanager

import com.google.gson.annotations.SerializedName

data class GetPriorityDTO(
    @SerializedName("versionCode")
    val versionCode:Int) {

    data class Response(
        @SerializedName("versionCode")
        val versionCode:Int,
        @SerializedName("priority")
        val priority:Int

    )
}