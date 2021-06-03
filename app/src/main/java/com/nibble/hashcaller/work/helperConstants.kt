package com.nibble.hashcaller.work

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.view.ui.contacts.utils.isNumericOnlyString

fun removeAllNonNumbericChars(str:String): String {

    return str.replace(Regex("[^0-9]"), "")
}

/**
 * @param1 phonenumber
 * @return formatted number without special symbols
 */
fun formatPhoneNumber(number:String): String {

    var numberCopy:String = ""
    numberCopy = number
    var fNum = ""
    val numWithoutSpceialChars = numberCopy.replace(Regex("[^A-Za-z0-9]"), "")

    if(isNumericOnlyString(numWithoutSpceialChars)){
        fNum = numberCopy.replace(Regex("[^A-Za-z0-9]"), "")
    }else{
        //not a numberic only type string, contains letters
       fNum = number.trim()

    }
    return fNum.trim()
}
/**
 * replces special characters with  space " ", eg: JL-JIOPAY to JL JIOPAY
 * @param1 phonenumber
 * @return formatted number without special symbols
 */
fun replaceSpecialChars(number:String): String {
    val fNum = number.replace(Regex("[^A-Za-z0-9]"), " ")
    return fNum.trim()
}

const val DESTINATION_ACTIVITY = "destinationFragment"
const val INDIVIDUAL_SMS_ACTIVITY = "individualSmSActivity"