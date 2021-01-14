package com.nibble.hashcaller.local.db.blocklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//Todo unique contact address
/**
 * table that stores all the spammers details recieved from server
 * include sms spammers and call spammers
 * title -> eg vodafone
 * stage_one_address -> phone number hashed result directly from client
 *
 */
@Entity(tableName = "spammer_info_from_server",indices = [Index(value =["contact_address"], unique = true)])
data class SpammersInfoFromServer(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "contact_address") val contactAddress: String?,
    @ColumnInfo(name = "type") val spammeerType: Int?,
    @ColumnInfo(name = "category") val category: Int?,
    @ColumnInfo(name = "title") val title:String
) {
}