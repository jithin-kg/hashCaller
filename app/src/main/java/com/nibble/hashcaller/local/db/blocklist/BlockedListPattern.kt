package com.nibble.hashcaller.local.db.blocklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG on 03,July,2020
 */
@Entity(tableName = "block_list_pattern", indices = [Index(value=["num_pattern", "num_pattern_regex"])])
data class BlockedListPattern(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="num_pattern") val numberPattern:String,
     @ColumnInfo(name = "num_pattern_regex") val numberPatterRegex:String   ) {
}