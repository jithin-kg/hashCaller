package com.nibble.hashcaller.view.ui.sms.db

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface ISMSThreadsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smsThreads: List<SmsThreadTable>)


    @Query("DELETE from chat_threads WHERE threadId=:threadId")
    suspend fun delete(threadId: Long)

    @Transaction
    @Query("SELECT * FROM chat_threads WHERE isDeleted=:isDeleted ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false): LiveData<MutableList<SMSThreadANDServerInfo>>

    @Query("SELECT * FROM chat_threads ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
    suspend fun getAll(): List<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<SmsThreadTable>>

    @Query("SELECT * FROM chat_threads WHERE contactAddress=:contactAddress")
    suspend fun find(contactAddress: String) : SmsThreadTable?

    @Query("DELETE from callers_info_from_server ")
    suspend fun deleteAll()

    @Query("UPDATE  chat_threads  SET name =:name, infoFoundFrom =:callerInfoFoundFrom  WHERE contactAddress =:contactAddress")
    suspend fun update(contactAddress: kotlin.String,  name:String, callerInfoFoundFrom: Int)

    @Query("UPDATE  chat_threads  SET isDeleted=:isDeleted WHERE threadId =:threadId")
    suspend fun markAsDeleted(threadId: Long, isDeleted:Boolean)
}