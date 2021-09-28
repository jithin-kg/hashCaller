package com.hashcaller.app.local.db.blocklist

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG on 03,July,2020
 * type indicates what is the pattern for
 *  0 -> starts with
 *  1 -> contains
 *  2 -> ends with
 *  3 -> exact number , number that user reported from call log, sms, individual contact views
 *  refer class BlockTypes for types
 */
@Keep
@Entity(tableName = "block_list_pattern", indices = [Index(value=["num_pattern", "num_pattern_regex"])])
data class BlockedListPattern(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="num_pattern") val numberPattern:String,
     @ColumnInfo(name = "num_pattern_regex") val numberPatterRegex:String,
     @ColumnInfo(name = "type") val type:Int   ) {
}