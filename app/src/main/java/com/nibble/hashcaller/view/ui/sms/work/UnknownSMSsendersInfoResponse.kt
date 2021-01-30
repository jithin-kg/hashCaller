package com.nibble.hashcaller.view.ui.sms.work

import androidx.annotation.Keep

@Keep
data class UnknownSMSsendersInfoResponse(
    val contacts: List<Contact>
)