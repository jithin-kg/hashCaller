package com.nibble.hashcaller.view.ui.call.db

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
interface CallersInfoFromServerDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(callersList: List<CallersInfoFromServer>)

    @Query("DELETE from callers_info_from_server WHERE contact_address=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM callers_info_from_server")
    fun getAllBLockListPattern(): LiveData<List<CallersInfoFromServer>>

    @Query("SELECT * FROM callers_info_from_server")
    suspend fun getAll(): List<CallersInfoFromServer>

    @Query("SELECT * FROM callers_info_from_server WHERE contact_address=:contactAddress")
    suspend fun find(contactAddress: String) : CallersInfoFromServer?
    @Query("DELETE from callers_info_from_server ")
    suspend fun deleteAll()

    @Query("UPDATE  callers_info_from_server  SET spamReportCount =:spamCount WHERE contact_address =:contactAddress")
    suspend fun update(spamCount: kotlin.Long, contactAddress: kotlin.String)
}