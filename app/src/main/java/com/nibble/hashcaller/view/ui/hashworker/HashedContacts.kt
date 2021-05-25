package com.nibble.hashcaller.view.ui.hashworker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table containing argon hashed numbers of numbers that are avialable in contacts
 * numHashed -> argon2 hashed number
 * number -> original formated number number
 * All the numbers in this table has to be uploaded to server and get information for
 * the  numbers
  */
@Entity(tableName = "hashed_number_contacts")
data class HashedContacts(
    @PrimaryKey() val number: String,
    @ColumnInfo(name="hashedNumber") val hashedNumber:String,

    ) {

}