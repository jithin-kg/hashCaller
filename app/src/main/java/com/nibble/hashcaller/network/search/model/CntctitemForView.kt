package com.nibble.hashcaller.network.search.model


import androidx.annotation.Keep
import com.nibble.hashcaller.network.StatusCodes.Companion.STATUS_SEARHING_IN_PROGRESS
import com.nibble.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER

@Keep
data class CntctitemForView(
    val firstName: String = "",

    val lastName: String = "",

    val carrier: String ="",

    val location: String ="",

    val lineType: String ="",

    val country: String="",

    val spammCount : Long = 0L,

    val statusCode:Int = STATUS_SEARHING_IN_PROGRESS,

    val isInfoFoundInDb:Int = INFO_NOT_FOUND_IN_SERVER // to indicate whether this caller info is available in server
    //if the caller information is not available in
    // server then we don't have to query again if  Date () - inforeceivedDate <=0
)