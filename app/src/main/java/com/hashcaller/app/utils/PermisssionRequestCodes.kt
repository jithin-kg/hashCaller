package com.hashcaller.app.utils

import androidx.annotation.Keep

@Keep
class PermisssionRequestCodes {
    companion object{
        const val REQUEST_CODE_READ_CONTACTS = 1
        const val READ_CNCT_DISPLAY_OVER = 7
        const val REQUEST_CODE_READ_PHONE_STATE = 3
        const val REQUEST_CODE_READ_SMS = 2
        const val REQUEST_CODE_RAD_CALLLOG_AND_READ_CONTACTS_PERMISSION =4
        const val REQUEST_CODE_WRITE_CALL_LOG = 5
        const val ROLE_SCREENING_APP_REQUEST_CODE =123
        const val REQUEST_CODE_STORAGE = 6

        const val REQUEST_CODE_CALL_LOG = 12

    }
}