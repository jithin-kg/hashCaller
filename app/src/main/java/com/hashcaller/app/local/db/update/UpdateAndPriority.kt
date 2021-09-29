package com.hashcaller.app.local.db.update

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * table containing update version code and its priority
 * priority ranges from 0 to 5
 * 5 immediate update
 */
@Keep
@Entity(tableName = "update_and_priority",indices = [Index(value =["version_code"], unique = true)])
data class UpdateAndPriority(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "version_code") val versionCode: Int,
    @ColumnInfo(name = "priority") val priority: Int,
) {
}