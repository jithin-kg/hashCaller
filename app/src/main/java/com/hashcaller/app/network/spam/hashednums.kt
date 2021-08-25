package com.hashcaller.app.network.spam

import androidx.annotation.Keep
import com.hashcaller.app.work.ContactAddressWithHashDTO

/**
 * for requesting multiple number search feature
 * contains array of hashedphonenumbers of type string
 *
 */
@Keep
data class hashednums(
    var hashedPhoneNum: List<ContactAddressWithHashDTO> = mutableListOf()
) {

}