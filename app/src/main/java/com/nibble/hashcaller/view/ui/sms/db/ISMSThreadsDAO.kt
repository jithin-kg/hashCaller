package com.nibble.hashcaller.view.ui.sms.db

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface ISMSThreadsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(smsThreads: List<SmsThreadTable>)


    @Query("DELETE from chat_threads WHERE threadId=:threadId")
    suspend fun delete(threadId: Long)

    @Query("SELECT * FROM chat_threads WHERE isDeleted=:isDeleted ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false): LiveData<MutableList<SmsThreadTable>>

    @Query("SELECT * FROM chat_threads ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
    suspend fun getAll(): List<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<SmsThreadTable>>

    @Query("SELECT * FROM chat_threads WHERE contactAddress=:contactAddress")
    suspend fun find(contactAddress: String) : SmsThreadTable?

    @Query("DELETE from chat_threads ")
    suspend fun deleteAll()

    @Query("UPDATE  chat_threads  SET name =:name, infoFoundFrom =:callerInfoFoundFrom  WHERE contactAddress =:contactAddress")
    suspend fun update(contactAddress: kotlin.String,  name:String, callerInfoFoundFrom: Int)

    @Query("UPDATE  chat_threads  SET isDeleted=:isDeleted WHERE threadId =:threadId")
    suspend fun markAsDeleted(threadId: Long, isDeleted:Boolean)

    @Query("UPDATE  chat_threads  SET read =:isRead WHERE contactAddress =:contactAddress")
    suspend fun markAsRead(contactAddress: String, isRead:Int)

    @Query("UPDATE  chat_threads  SET spamCountFromServer =:spamReportCount, nameFromServer =:name WHERE contactAddress =:contactAddress")
    suspend fun updateWithServerInfo(contactAddress: String, spamReportCount: Long, name: String)

    @Query("UPDATE  chat_threads  SET body =:body, dateInMilliseconds =:dateInMilliseconds WHERE contactAddress =:contactAddress")
    suspend fun updateBodyAndContents(contactAddress: String, body: String, dateInMilliseconds: Long)

    @Query("UPDATE  chat_threads  SET spamCountFromServer =:spamCountFromServer, name =:name, nameFromServer=:nameFromServer WHERE contactAddress =:contactAddress")
    suspend fun updateInfos(contactAddress: String, spamCountFromServer: Long, name: String, nameFromServer: String)
}