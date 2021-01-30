package com.nibble.hashcaller.work


/**
 * @param1 phonenumber
 * @return formatted number without special symbols
 */
fun formatPhoneNumber(number:String): String {
    val fNum = number.replace(Regex("[^A-Za-z0-9]"), "")
    return fNum
}

const val DESTINATION_ACTIVITY = "destinationFragment"
const val INDIVIDUAL_SMS_ACTIVITY = "individualSmSActivity"