package com.nibble.hashcaller.utils.constants

import androidx.annotation.Keep

@Keep
class IntentKeys {
    companion object{
        //incomming call view Activity
        const val FIRST_NAME = "fistName"
        const val LAST_NAME = "lastName"
        const val PHONE_NUMBER = "phoneNumber"
        const val SPAM_COUNT = "spamcount"
        const val CARRIER = "carrier"
        const val LOCATION = "location"
        const val COUNTRY = "coutry"
        const val STATUS_CODE = "status_code"
        const val SHOW_FEEDBACK_VIEW = "show_feedback_view" // indicate whether show feedback view , boolean
        const val EXPAND_INCOMMING_VIEW = "com.nibble.hashcaller.expand_incomming_view" // these are send thrugh broadcast receiver
        const val UPDATE_INCOMMING_VIEW = "com.nibble.hashcaller.update_incomming_view" // these are send thrugh broadcast receiver
        const val CLOSE_INCOMMING_VIEW = "com.nibble.hashcaller.close_incomming_view"
        const val INTENT_COMMAND = "com.nibble.hashcaller.incommingCallintent"
        const val STOP_FLOATING_SERVICE = "com.nibble.stop_floating_service"
        const val STOP_FLOATING_SERVICE_AND_WINDOW = "com.nibble.close_service_n_window"
        const val START_FLOATING_SERVICE = "com.nibble.start_floatin_window"
        const val START_FLOATING_SERVICE_FROM_SCREENING_SERVICE = "com.nibble.start_floatin_window_from_screening"
    }
}