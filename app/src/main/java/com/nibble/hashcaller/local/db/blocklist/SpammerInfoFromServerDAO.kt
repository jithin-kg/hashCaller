package com.nibble.hashcaller.local.db.blocklist

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
interface SpammerInfoFromServerDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(spamerlist: List<SpammersInfoFromServer>)

    @Query("DELETE from spammer_info_from_server WHERE contact_address=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM spammer_info_from_server")
    fun getAllBLockListPattern(): LiveData<List<SpammersInfoFromServer>>

    @Query("SELECT * FROM spammer_info_from_server")
    suspend fun getAll(): List<SpammersInfoFromServer>

    @Query("SELECT * FROM spammer_info_from_server WHERE contact_address=:contactAddress")
    suspend fun get(contactAddress: String) : SpammersInfoFromServer
}