package com.nibble.hashcaller.local.db.contactInformation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG on 01,August,2020
 */
@Entity(tableName = "contacts_information")
data class ContactTable(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="number") val number:String,
    @ColumnInfo(name = "name") val name:String   ) {
}