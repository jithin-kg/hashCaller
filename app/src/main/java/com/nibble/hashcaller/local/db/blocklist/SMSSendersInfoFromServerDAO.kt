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
interface SMSSendersInfoFromServerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spamerlist: List<SMSSendersInfoFromServer>)

    @Query("DELETE from sms_senders_info_from_server WHERE contact_address=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM sms_senders_info_from_server")
    fun getAllBLockListPattern(): LiveData<List<SMSSendersInfoFromServer>>

    @Query("SELECT * FROM sms_senders_info_from_server")
    suspend fun getAll(): List<SMSSendersInfoFromServer>

    @Query("SELECT * FROM sms_senders_info_from_server")
    fun getAllLiveData(): LiveData<List<SMSSendersInfoFromServer>>

    @Query("SELECT * FROM sms_senders_info_from_server WHERE contact_address=:contactAddress")
    suspend fun find(contactAddress: String) : SMSSendersInfoFromServer?
    @Query("DELETE FROM sms_senders_info_from_server")
    suspend fun deleteAll()
}