package com.hashcaller.app.local.db.sms

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "sms_outbox")
data class SMSOutBox( @PrimaryKey() val id: Int?) {

}