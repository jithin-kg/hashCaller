package com.nibble.hashcaller.view.ui.call.db

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface ICallLogDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(callersList: List<CallLogTable>)


    @Query("DELETE from call_log WHERE id=:id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("SELECT * FROM call_log WHERE isDeleted=:isDeleted ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false): LiveData<List<CallLogAndInfoFromServer>>

    @Query("SELECT * FROM call_log ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<CallLogAndInfoFromServer>

    @Query("SELECT * FROM call_log")
    suspend fun getAll(): List<CallLogTable>

    @Query("SELECT * FROM call_log")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallLogAndInfoFromServer>>

    @Query("SELECT * FROM call_log WHERE number=:contactAddress")
    suspend fun find(contactAddress: String) : CallLogAndInfoFromServer?

    @Query("DELETE from call_log ")
    suspend fun deleteAll()

    @Query("UPDATE  call_log  SET name =:name, callerInfoFoundFrom =:callerInfoFoundFrom  WHERE number =:contactAddress")
    suspend fun update(contactAddress: kotlin.String,  name:String, callerInfoFoundFrom: Int)

    @Query("UPDATE  call_log  SET isDeleted=:isDeleted WHERE id =:id")
    suspend fun markAsDeleted(id: Long, isDeleted:Boolean)
}