package com.nibble.hashcaller.view.ui.call.db

import androidx.room.*
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

//Todo unique contact address
/**
 *table containing all call logs with all met info about that call
 *
 */

//TODO save the

@Entity(tableName = "call_log",indices = [Index(value =["id", "number"], unique = true)])
 data class CallLogTable (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") var id: Long?,
    @ColumnInfo(name = "name") var name:String? = null,
    @ColumnInfo(name = "nameFromServer") var nameFromServer:String? = null,
    @ColumnInfo(name = "spamCount") var spamCount: Long = 0,
    @ColumnInfo(name = "number") var  number:String = "",
    @ColumnInfo(name = "numberFormated") var  numberFormated:String = "",
    @ColumnInfo(name = "type") var type : Int = 0,
    @ColumnInfo(name = "duration") var duration: String = "",
    @ColumnInfo(name = "dateInMilliseconds") var dateInMilliseconds: Long  = 0L,
    @ColumnInfo(name = "color") var color: Int = 0,
    @ColumnInfo(name ="simID") var simId:Int = 1,
    @ColumnInfo(name = "thumbnailFromCp") var thumbnailFromCp: String = "",
    @ColumnInfo(name = "imageUrlFromDb") var imageFromDb: String = "",
    @ColumnInfo(name = "isDeleted") var isDeleted: Boolean = false) {
}