package com.nibble.hashcaller.local.db.blocklist.mutedCallers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table to keep track of muted contact address
 * For muted contact addresses no sound or  notification will be shown
 */
@Entity(tableName = "muted_callers")
data class MutedCallers(
    @PrimaryKey( autoGenerate = false) val address:String
    ) {
}