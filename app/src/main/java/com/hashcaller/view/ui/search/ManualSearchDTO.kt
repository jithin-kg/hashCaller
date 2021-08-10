package com.hashcaller.view.ui.search

import androidx.annotation.Keep

@Keep
data class ManualSearchDTO(
    var phoneNumber:String="",
    var countryCode: String ="",
    var countryIso: String = ""
   ) {
}