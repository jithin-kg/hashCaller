package com.nibble.hashcaller.view.ui.sms.db

import androidx.room.Embedded
import androidx.room.Relation
import com.google.gson.Gson
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer


/**
 * joined table of smsthread and server info
 */
data class SMSThreadANDServerInfo(
    @Embedded val smsThreadTable: SmsThreadTable,
    @Relation(parentColumn = "contactAddress", entityColumn = "contact_address")val smsenderInfoFromServer: SMSSendersInfoFromServer?
)
{

    fun deepCopy() : SMSThreadANDServerInfo {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }
}