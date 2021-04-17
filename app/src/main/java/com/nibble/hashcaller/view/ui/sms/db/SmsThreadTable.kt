package com.nibble.hashcaller.view.ui.sms.db

import androidx.room.*
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

//Todo unique contact address
/**
 *table containing chat threads
 *
 */

//TODO save the

@Entity(tableName = "chat_threads",indices = [Index(value =["threadId", "contactAddress"], unique = true)])
 data class SmsThreadTable (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "threadId") var threadId: Long = 0,
    @ColumnInfo(name = "name") var name:String ="",
    @ColumnInfo(name = "nameFromServer") var nameFromServer:String? = null,
    @ColumnInfo(name = "spamCountFromServer") var spamCountFromServer:Long = 0,
    @ColumnInfo(name = "contactAddress") var  contactAddress:String = "",
    @ColumnInfo(name = "type") var type : Int = 0,
    @ColumnInfo(name = "read") var readState: Int = 0,
    @ColumnInfo(name = "body") var body: String = "",
    @ColumnInfo(name = "folderName") var folderName: String = "",
    @ColumnInfo(name = "dateInMilliseconds") var dateInMilliseconds: Long  = 0L,
    @ColumnInfo(name = "infoFoundFrom") var senderInfoFoundFrom: Int = 0,
    @ColumnInfo(name = "isDeleted") var isDeleted: Boolean = false) {
}