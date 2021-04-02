package com.nibble.hashcaller.view.ui.call.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intellij.lang.annotations.Flow

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

    @Query("SELECT * FROM callers_info_from_server")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallersInfoFromServer>>

    @Query("SELECT * FROM callers_info_from_server WHERE contact_address=:contactAddress")
    suspend fun find(contactAddress: String) : CallersInfoFromServer?
    @Query("DELETE from callers_info_from_server ")
    suspend fun deleteAll()

    @Query("UPDATE  callers_info_from_server  SET spamReportCount =:spamCount,isBlockedByUser =:isBlockedByUser  WHERE contact_address =:contactAddress ")
    suspend fun update(spamCount: kotlin.Long, contactAddress: kotlin.String, isBlockedByUser:Boolean)

    @Query("UPDATE  callers_info_from_server  SET isBlockedByUser =:isBlockedByUser WHERE contact_address =:contactAddress")
    suspend fun unBlock(isBlockedByUser:Boolean, contactAddress: kotlin.String)

    @Query("SELECT * FROM callers_info_from_server")
    fun getAllLiveData()  : LiveData<List<CallersInfoFromServer>>
}