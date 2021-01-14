package com.nibble.hashcaller.network.spam

import androidx.annotation.Keep

/**
 * for requesting multiple number search feature
 * contains array of hashedphonenumbers of type string
 */
@Keep
data class hashednums(
    var hashedPhoneNum: MutableList<String> = mutableListOf()
) {

}