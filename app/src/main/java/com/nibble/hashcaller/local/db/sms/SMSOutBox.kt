package com.nibble.hashcaller.local.db.sms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_outbox")
data class SMSOutBox( @PrimaryKey() val id: Int?) {

}