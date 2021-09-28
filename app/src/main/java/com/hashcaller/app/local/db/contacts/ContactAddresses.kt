package com.hashcaller.app.local.db.contacts

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Jithin KG
 * this table is used to store all formated  phone numbers into db
 * so that we can search and compare while incomming call comes and
 * block non contact calls if user enabled that option
 *
 */
@Keep
@Entity(tableName = "contact_addresses_for_searching")
data class ContactAddresses(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "number")
    val number: String) {
}