package com.hashcaller.app.network.search.model


import androidx.annotation.Keep
import com.hashcaller.app.network.HttpStatusCodes.Companion.STATUS_SEARHING_IN_PROGRESS
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import java.util.*

@Keep
data class CntctitemForView(
    var firstName: String = "",

    var lastName: String = "",

    var carrier: String ="",

    var location: String ="",

    var lineType: String ="",

    var country: String="",
    var informationReceivedDate:Date,
    var spammCount : Long = 0L,
    var thumbnailImg:String = "",
    var statusCode:Int = STATUS_SEARHING_IN_PROGRESS,

    var isInfoFoundInServer:Int = INFO_NOT_FOUND_IN_SERVER, // to indicate whether this caller info is available in server
    //if the caller information is not available in
    // server then we don't have to query again if  Date () - inforeceivedDate <=0

    // when an incomming call comes there is a condition in which sometimes network connection avialble sometimes not
    //if network avaialble and got a reponse for the incomming caller this should be true
    var isSearchedForCallerInserver:Boolean = false,
    var isInInContacts:Boolean = false

)