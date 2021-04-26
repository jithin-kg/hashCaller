package com.nibble.hashcaller.view.ui.sms.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface ISMSThreadsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(smsThreads: List<SmsThreadTable>)

    @Query("SELECT * FROM chat_threads WHERE isDeleted=:isDeleted  AND isReportedByUser=:isReportedByUser ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false, isReportedByUser:Boolean = false): LiveData<MutableList<SmsThreadTable>>

    @Query("SELECT * FROM chat_threads WHERE isDeleted=:isDeleted  AND isReportedByUser=:isReportedByUser OR spamCount > :spsmCountLimit ORDER BY dateInMilliseconds DESC ")
    fun getSpamSMSLogLivedata(isDeleted: Boolean=false, isReportedByUser: Boolean = true, spsmCountLimit: Long = SPAM_THREASHOLD): LiveData<MutableList<SmsThreadTable>>

    @Query("DELETE from chat_threads WHERE threadId=:threadId")
    suspend fun delete(threadId: Long)

    @Query("SELECT * FROM chat_threads ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
    suspend fun getAll(): List<SmsThreadTable>

    @Query("SELECT * FROM chat_threads")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<SmsThreadTable>>

    @Query("SELECT * FROM chat_threads WHERE numFormated=:contactAddress")
    suspend fun find(contactAddress: String) : SmsThreadTable?

    @Query("DELETE from chat_threads ")
    suspend fun deleteAll()

    @Query("UPDATE  chat_threads  SET name =:name, infoFoundFrom =:callerInfoFoundFrom  WHERE numFormated =:contactAddress")
    suspend fun update(contactAddress: kotlin.String,  name:String, callerInfoFoundFrom: Int)

    @Query("UPDATE  chat_threads  SET isDeleted=:isDeleted WHERE threadId =:threadId")
    suspend fun markAsDeleted(threadId: Long, isDeleted:Boolean=true)

    @Query("UPDATE  chat_threads  SET read =:isRead WHERE numFormated =:contactAddress")
    suspend fun markAsRead(contactAddress: String, isRead:Int)

    @Query("UPDATE  chat_threads  SET spamCount =:spamReportCount, nameFromServer =:name WHERE numFormated =:contactAddress")
    suspend fun updateWithServerInfo(contactAddress: String, spamReportCount: Long, name: String)

    @Query("UPDATE  chat_threads  SET body =:body, dateInMilliseconds =:dateInMilliseconds WHERE numFormated =:contactAddress")
    suspend fun updateBodyAndContents(contactAddress: String, body: String, dateInMilliseconds: Long)

    @Query("UPDATE  chat_threads  SET spamCount =:spamCountFromServer, name =:name, nameFromServer=:nameFromServer,thumbnailFromCp =:thumbnailFromCp  WHERE numFormated =:contactAddress")
    suspend fun updateInfos(
        contactAddress: String,
        spamCountFromServer: Long,
        name: String,
        nameFromServer: String?,
        thumbnailFromCp: String
    )

    @Query("SELECT * FROM chat_threads WHERE name like :searchQuery")
    suspend fun findNameLike(searchQuery: String?): List<SmsThreadTable>?

    @Query("UPDATE  chat_threads  SET spamCount =1+spamCount, isReportedByUser =:reportedByUser WHERE numFormated =:contactAddress")
    suspend fun updateSpamCount(contactAddress: String, reportedByUser: Boolean=true)

    @Query("SELECT * FROM chat_threads WHERE threadId  =:id LIMIT 1")
    suspend fun findOneById(id: Long) : SmsThreadTable?


}