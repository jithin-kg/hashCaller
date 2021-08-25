package com.hashcaller.app.view.ui.hashworker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table containing argon hashed numbers of numbers that are not in contacts
 * numHashed -> argon2 hashed number
 * number -> original formated number number
 * All the numbers in this table has to be uploaded to server and get information for
 * the  numbers
  */
@Entity(tableName = "unknown_hashed_number")
data class HashedNumber(
    @PrimaryKey() val number: String,
    @ColumnInfo(name="hashedNumber") val hashedNumber:String,

    ) {

}