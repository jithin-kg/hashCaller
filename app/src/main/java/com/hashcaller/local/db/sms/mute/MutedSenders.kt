package com.hashcaller.local.db.sms.mute

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table to keep track of muted contact address
 * For muted contact addresses no notification will be shown
 */
@Entity(tableName = "muted_senders")
data class MutedSenders(
    @PrimaryKey( autoGenerate = false) val address:String
    ) {
}