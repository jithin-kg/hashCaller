package com.nibble.hashcaller.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG on 03,July,2020
 */
@Entity(tableName = "block_list_pattern")
data class BlockedListPattern(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="num_pattern") val numberPattern:String,
     @ColumnInfo(name = "num_pattern_regex") val numberPatterRegex:String   ) {
}