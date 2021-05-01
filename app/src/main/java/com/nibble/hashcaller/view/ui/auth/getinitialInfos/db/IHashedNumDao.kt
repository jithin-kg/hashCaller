package com.nibble.hashcaller.view.ui.auth.getinitialInfos.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface IHashedNumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserHashedNumber)

    @Query("DELETE from user_hashed_number")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_hashed_number LIMIT 1")
    fun getHashLiveData(): LiveData<UserHashedNumber>

    @Query("SELECT * FROM user_hashed_number LIMIT 1")
    suspend fun getHash(): UserHashedNumber?
}