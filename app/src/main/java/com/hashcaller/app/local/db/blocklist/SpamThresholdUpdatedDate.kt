package com.hashcaller.app.local.db.blocklist

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 *
 * table keep track of last date in which spam threshold is
 * updated date
 */
@Keep
@Entity(tableName = "spam_threshold_update_date")
data class SpamThresholdUpdatedDate(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="date") val date: Date
    ) {
}