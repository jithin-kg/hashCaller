package com.hashcaller.app.view.ui.call.individualCallLog

/**
 * The ISO 3166-1 two letters country code of the country where the
 * user received or made the call.
 * <P>
 * Type: String
 * </P>
 */

/**
 * A geocoded location for the number associated with this call.
 * <p>
 * The string represents a city, state, or country associated with the number.
 * <P>Type: TEXT</P>
 */

/**
 * The identifier for the account used to place or receive the call.
 * <P>Type: TEXT</P>
 */

data class IndividualCallLogObj(
    val id:Int? = null,
    val number: String? = null,
    val name : String? = null,
    val type: Int = 0,          //direction
    val duration: Long = 0L,
    val countryIso:String? = null,
    val geocodedLocation:String? = null,
    val subscription_id:String? = null,
    val photo_uri:String? = null,
    val date:Long? = null

) {
}