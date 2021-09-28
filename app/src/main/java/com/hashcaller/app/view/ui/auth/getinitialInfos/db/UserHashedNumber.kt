package com.hashcaller.app.view.ui.auth.getinitialInfos.db

import androidx.annotation.Keep
import androidx.room.*

//Todo unique contact address
/**
 * table that stores user entered phone number as hash
 */
@Keep
@Entity(tableName = "user_hashed_number",indices = [Index(value =["hashed_num"], unique = true)])
data class UserHashedNumber (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "hashed_num") val hashedNumber: String,
    @ColumnInfo(name = "phone_num") val phoneNumber: String



) {
}