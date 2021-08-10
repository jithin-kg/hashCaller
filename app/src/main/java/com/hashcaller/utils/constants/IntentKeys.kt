package com.hashcaller.utils.constants

import androidx.annotation.Keep

@Keep
class IntentKeys {
    companion object{
        //incomming call view Activity
        const val FULL_NAME_IN_C_PROVIDER = "fullName"
        const val FULL_NAME_FROM_SERVER = "nameFromServer"
        const val THUMBNAIL_FROM_CPROVIDER = "thumbnailFromCProvider"
        const val THUMBNAIL_FROM_BB = "thumbnailFromDatabase"
        const val IS_REPORTED_BY_USER = "isReportedByUser"
        const val AVATAR_COLOR = "color"
        const val FIRST_NAME = "fistName"
        const val LAST_NAME = "lastName"
        const val PHONE_NUMBER = "phoneNumber"
        const val SPAM_COUNT = "spamcount"
        const val CARRIER = "carrier"
        const val LOCATION = "location"
        const val COUNTRY = "coutry"
        const val STATUS_CODE = "status_code"
        const val SHOW_FEEDBACK_VIEW = "show_feedback_view" // indicate whether show feedback view , boolean
        const val EXPAND_INCOMMING_VIEW = "com.hashcaller.expand_incomming_view" // these are send thrugh broadcast receiver
        const val UPDATE_INCOMMING_VIEW = "com.hashcaller.update_incomming_view" // these are send thrugh broadcast receiver
        const val CLOSE_INCOMMING_VIEW = "com.hashcaller.close_incomming_view"
        const val INTENT_COMMAND = "com.hashcaller.incommingCallintent"
        const val STOP_FLOATING_SERVICE = "com.nibble.stop_floating_service"
        const val STOP_FLOATING_SERVICE_AND_WINDOW = "com.nibble.close_service_n_window"
        const val STOP_FLOATING_SERVICE_FROM_INCOMMING_ACTVTY = "c.h.stopPltIncomAct"
        const val STOP_FLOATIN_SERVICE_FROM_RECEIVER = "c.h.stopFltFromReceiever"
        const val START_FLOATING_SERVICE = "com.nibble.start_floatin_window"
        const val START_FLOATING_SERVICE_OFF_HOOK = "com.nibble.start_floatin_offhook"
        const val START_FLOATING_SERVICE_FROM_SCREENING_SERVICE = "com.nibble.start_floatin_window_from_screening"
        const val BROADCAST_STOP_FLOATING_SERVICE = "com.hashcaller.stop_floating"
        const val SHOW_BLOCK_LIST = "showBlockList" // to how blocklistframgnet in mainactivity
        const val SHOW_BLOCK_LIST_VALUE = 14
        const val CALL_STATE = "c.h.callState"
        const val CALL_HANDLED_STATE = "c.h.callHandldState"
        const val CALL_HANDLED_SIM = "c.h.callHndledSim"
    }
}