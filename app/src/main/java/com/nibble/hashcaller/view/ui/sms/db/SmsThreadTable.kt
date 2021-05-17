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
   @ColumnInfo(name = "firstName") var firstName:String ="",
   @ColumnInfo(name = "lastName") var lastName:String ="",
   @ColumnInfo(name = "firstNameFromServer") var firstNameFromServer:String? = null,
   @ColumnInfo(name = "lastNameFromServer") var lastNameFromServer:String? = null,
   @ColumnInfo(name = "spamCount") var spamCount:Long = 0,
   @ColumnInfo(name = "contactAddress") var  contactAddress:String = "",
   @ColumnInfo(name = "numFormated") var  numFormated:String = "",
   @ColumnInfo(name = "type") var type : Int = 0,
   @ColumnInfo(name = "read") var readState: Int = 0,
   @ColumnInfo(name = "body") var body: String = "",
   @ColumnInfo(name ="isReportedByUser") var isReportedByUser:Boolean = false,
   @ColumnInfo(name = "folderName") var folderName: String = "",
   @ColumnInfo(name = "dateInMilliseconds") var dateInMilliseconds: Long  = 0L,
   @ColumnInfo(name = "infoFoundFrom") var senderInfoFoundFrom: Int = 0,
   @ColumnInfo(name = "thumbnailFromCp") var thumbnailFromCp: String = "",
   @ColumnInfo(name = "imageUrlFromDb") var imageFromDb: String = "", // imageUrlFromDb is the image from server
   @ColumnInfo(name = "isDeleted") var isDeleted: Boolean = false) {
}