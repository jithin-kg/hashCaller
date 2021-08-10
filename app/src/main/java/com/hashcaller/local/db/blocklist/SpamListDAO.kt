package com.hashcaller.local.db.blocklist

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
interface SpamListDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(spammerInfo: SpammerInfo)

    @Query("DELETE from spammer_info WHERE contact_address=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM spammer_info")
    fun getAllBLockListPattern(): LiveData<List<SpammerInfo>>

    @Query("SELECT * FROM spammer_info")
    suspend fun getAll(): List<SpammerInfo>

    @Query("SELECT * FROM spammer_info WHERE contact_address=:contactAddress")
    suspend fun get(contactAddress: String) : SpammerInfo
}