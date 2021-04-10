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
interface ICallLogDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(callersList: List<CallLogTable>)

    @Query("DELETE from call_log WHERE number=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM call_log ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(): LiveData<List<CallLogTable>>

    @Query("SELECT * FROM call_log")
    suspend fun getAll(): List<CallLogTable>

    @Query("SELECT * FROM call_log")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallLogTable>>

    @Query("SELECT * FROM call_log WHERE number=:contactAddress")
    suspend fun find(contactAddress: String) : CallLogTable?
    @Query("DELETE from callers_info_from_server ")
    suspend fun deleteAll()

    @Query("UPDATE  call_log  SET spamReportCount =:spamCount,number =:isBlockedByUser  WHERE number =:contactAddress ")
    suspend fun update(spamCount: kotlin.Long, contactAddress: kotlin.String, isBlockedByUser:Boolean)


}