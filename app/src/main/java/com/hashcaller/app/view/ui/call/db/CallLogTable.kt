package com.hashcaller.app.view.ui.call.db

import androidx.room.*

//Todo unique contact address
/**
 *table containing all call logs with all met info about that call
 *number formated and number feilds are needed, becase while deleting call log non formated number is needed to perform deletion.
 *
 */

//TODO save the

@Entity(tableName = "call_log",indices = [Index(value =["id", "number"], unique = true)])
 data class CallLogTable (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "number") var  number:String = "", // number should be primary key, else there is duplicates
    @ColumnInfo(name = "id") var id: Long?,
    @ColumnInfo(name = "nameInPhoneBook") var nameInPhoneBook:String? = null,
//    nameFromServer is the first name and last name combined name that received from server
    @ColumnInfo(name = "nameFromServer") var nameFromServer:String? = null,
    @ColumnInfo(name = "spamCount") var spamCount: Long = 0,
    @ColumnInfo(name = "numberFormated") var  numberFormated:String = "",
    @ColumnInfo(name = "type") var type : Int = 0,
    @ColumnInfo(name = "duration") var duration: String = "",
    @ColumnInfo(name = "dateInMilliseconds") var dateInMilliseconds: Long  = 0L,
    @ColumnInfo(name = "color") var color: Int = 0,
    @ColumnInfo(name ="simID") var simId:Int = 1,
    @ColumnInfo(name ="isReportedByUser") var isReportedByUser:Boolean = false,
    @ColumnInfo(name = "thumbnailFromCp") var thumbnailFromCp: String = "",
    @ColumnInfo(name = "imageUrlFromDb") var imageFromDb: String = "",
    @ColumnInfo(name = "isDeleted") var isDeleted: Boolean = false,
    @ColumnInfo(name = "hUid") var hUid: String,

    ) {
}