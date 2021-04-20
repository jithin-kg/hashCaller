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

    @Query("SELECT * FROM call_log WHERE isDeleted=:isDeleted AND isReportedByUser =:isReportedByUser ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false, isReportedByUser: Boolean = false): LiveData<MutableList<CallLogTable>>

    @Query("SELECT * FROM call_log ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<CallLogTable>

    @Query("SELECT * FROM call_log")
    suspend fun getAllForDeleting(): List<CallLogTable>

    @Query("SELECT * FROM call_log")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallLogTable>>

    @Query("SELECT * FROM call_log WHERE number=:contactAddress")
    suspend fun find(contactAddress: String) : CallLogTable?
    @Query("SELECT * FROM call_log WHERE number=:contactAddress LIMIT 1")
    suspend fun findOne(contactAddress: String) : CallLogTable?

    @Query("DELETE from call_log ")
    suspend fun deleteAll()

    @Query("UPDATE  call_log  SET nameFromServer =:nameFromServer, spamCount =:spamCount  WHERE number =:contactAddress")
    suspend fun updateWitServerInfo(contactAddress: kotlin.String, nameFromServer:String, spamCount: kotlin.Long)

    @Query("UPDATE  call_log  SET name =:name, thumbnailFromCp=:thumbnailFromCp WHERE number =:contactAddress")
    suspend fun updateWitCproviderInfo(contactAddress: String, name:String, thumbnailFromCp: String)

    @Query("UPDATE  call_log  SET isDeleted=:isDeleted WHERE id =:id")
    suspend fun markAsDeleted(id: Long, isDeleted:Boolean)

    @Query("SELECT * FROM call_log WHERE isDeleted=:isDeleted AND  isReportedByUser =:isReportedByUser ORDER BY dateInMilliseconds DESC LIMIT 10")
    suspend fun getFirst10Logs(isDeleted: Boolean = false, isReportedByUser:Boolean= false) : MutableList<CallLogTable>

    @Query("SELECT * FROM call_log WHERE number LIKE :contactAddress OR name LIKE :contactAddress OR nameFromServer LIKE :contactAddress ORDER BY dateInMilliseconds DESC")
    suspend fun searchCalllog(contactAddress: String): MutableList<CallLogTable>

    @Query("UPDATE  call_log  SET isReportedByUser=:isReportedByUser, spamCount =:spamCount WHERE number =:contactAddress")
    suspend fun markAsReportedByUser(contactAddress: String, spamCount: Long, isReportedByUser:Boolean = true)
}