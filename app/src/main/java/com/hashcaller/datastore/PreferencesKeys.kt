package com.hashcaller.datastore

import androidx.annotation.Keep

@Keep
class PreferencesKeys {
    companion object{
        const val TOKEN = "token"
        const val KEY_BLOCK_COMMONG_SPAMMERS = "block_commong_spammer"
        const val KEY_BLOCK_FOREIGN_NUMBER = "block_foreign_numbers"
        const val KEY_BLOCK_NON_CONTACT = "block_non_contact_numbers"
        const val SHOW_SMS_IN_SEARCH_RESULT = "show_sms_search_result"
        const val USER_INFO_AVIALABLE_IN_DB ="user_info_avialable_in_db"
        const val DO_NOT_RECIEVE_SPAM_SMS = "doNotReceiveSpamSMS"

        const val RCV_NOT_BLK_CALL ="rcvntoForBlkCall"
        const val RCV_NOT_BLK_SMS ="rcvntoForBlkSMS"
    }
}