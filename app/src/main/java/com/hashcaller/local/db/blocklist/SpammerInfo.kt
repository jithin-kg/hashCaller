package com.hashcaller.local.db.blocklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//Todo unique contact address
/**
 * class to insert user spam reported numbers
 */
@Entity(tableName = "spammer_info",indices = [Index(value =["contact_address"], unique = true)])
data class SpammerInfo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "contact_address") val contactAddress: String?,
    @ColumnInfo(name = "type") val spammeerType: Int?,
    @ColumnInfo(name = "category") val category: Int?,
    @ColumnInfo(name = "thread_id") val threadId:Long
) {
}