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
    val thumbnailImg:String = "",
    val statusCode:Int = STATUS_SEARHING_IN_PROGRESS,

    val isInfoFoundInServer:Int = INFO_NOT_FOUND_IN_SERVER, // to indicate whether this caller info is available in server
    //if the caller information is not available in
    // server then we don't have to query again if  Date () - inforeceivedDate <=0



    // when an incomming call comes there is a condition in which sometimes network connection avialble sometimes not
    //if network avaialble and got a reponse for the incomming caller this should be true
    val isSearchedForCallerInserver:Boolean = false
)