package com.nibble.hashcaller.local.db.sms.block

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table to keep track of blocked or spam contact address
 * For  blocked contact address we do not even do not call saveSmsInInbox(context, currentSMS) in SmsReceiver
 */
@Entity(tableName = "blocked_or_spam_senders")
data class BlockedOrSpamSenders(
    @PrimaryKey( autoGenerate = false) val address:String,
    @ColumnInfo(name = "spamCount") var spamCount:Long = 0L,
    @ColumnInfo(name = "type") var spammerType:String = "",
    @ColumnInfo(name="country") var country:String = ""
    ) {
}