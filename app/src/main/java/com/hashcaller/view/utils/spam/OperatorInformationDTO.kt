package com.hashcaller.view.utils.spam

import androidx.annotation.Keep

@Keep
data class OperatorInformationDTO(
    var opearatorDisplayName:String = "",
    var countryIso:String = ""
    ) {
}