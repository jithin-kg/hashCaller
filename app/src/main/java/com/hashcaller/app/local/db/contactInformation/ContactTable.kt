package com.hashcaller.app.local.db.contactInformation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG on 01,August,2020
 * table to save contact address and its name and meta information about a number
 */
@Entity(tableName = "contacts_information", indices = [Index(value = ["number"], unique = true)])
data class ContactTable(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="number") val number:String,
    @ColumnInfo(name = "name") val name:String,
    @ColumnInfo(name = "carrier") val carrier:String,
    @ColumnInfo(name = "location") val location:String,
    @ColumnInfo(name = "country") val country:String,
    @ColumnInfo(name = "spamCount") val spamCount: Int

    ) {
}