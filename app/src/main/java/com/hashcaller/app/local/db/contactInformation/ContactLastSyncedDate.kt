package com.hashcaller.app.local.db.contactInformation

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Jithin KG on 01,August,2020
 */
@Keep
@Entity(tableName = "contact_last_synced_date")
data class ContactLastSyncedDate(
    @PrimaryKey(autoGenerate = true) val id: Int?,

    @ColumnInfo(name="date") val date:Date

) {
}