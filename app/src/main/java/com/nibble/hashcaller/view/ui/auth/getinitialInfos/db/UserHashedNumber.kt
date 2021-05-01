package com.nibble.hashcaller.view.ui.auth.getinitialInfos.db

import androidx.room.*
import java.time.OffsetDateTime
import java.util.*

//Todo unique contact address
/**
 * table that stores user entered phone number as hash
 */

@Entity(tableName = "user_hashed_number",indices = [Index(value =["hashed_num"], unique = true)])
data class UserHashedNumber (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "hashed_num") val hashedNumber: String,
    @ColumnInfo(name = "phone_num") val phoneNumber: String



) {
}