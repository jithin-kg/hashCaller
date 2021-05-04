package com.nibble.hashcaller.view.ui.call.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_SPAM

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

    @Query("SELECT * FROM call_log WHERE (isDeleted=:isDeleted AND isReportedByUser =:isReportedByUser )  AND spamCount <=:spamLimit ORDER BY dateInMilliseconds DESC ")
    fun getAllLiveData(isDeleted:Boolean= false, isReportedByUser: Boolean = false, spamLimit: Long = SPAM_THREASHOLD): LiveData<MutableList<CallLogTable>>

    @Query("SELECT * FROM call_log WHERE isDeleted=:isDeleted AND  isReportedByUser =:isReportedByUser  AND name!=''  AND spamCount <=:spamLimit  ORDER BY dateInMilliseconds DESC LIMIT 10")
    suspend fun getFirst10Logs(isDeleted: Boolean = false, isReportedByUser:Boolean= false, spamLimit: Long = SPAM_THREASHOLD) : MutableList<CallLogTable>

    @Query("SELECT * FROM call_log ORDER BY dateInMilliseconds DESC ")
    suspend fun getAllCallLog(): MutableList<CallLogTable>

    @Query("SELECT * FROM call_log")
    suspend fun getAllForDeleting(): List<CallLogTable>

    @Query("SELECT * FROM call_log")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallLogTable>>

    @Query("SELECT * FROM call_log WHERE numberFormated=:contactAddress")
    suspend fun find(contactAddress: String) : CallLogTable?
    @Query("SELECT * FROM call_log WHERE numberFormated=:contactAddress LIMIT 1")
    suspend fun findOne(contactAddress: String) : CallLogTable?

    @Query("DELETE from call_log ")
    suspend fun deleteAll()

    @Query("UPDATE  call_log  SET nameFromServer =:nameFromServer, spamCount =:spamCount  WHERE numberFormated =:contactAddress")
    suspend fun updateWitServerInfo(contactAddress: kotlin.String, nameFromServer:String, spamCount: kotlin.Long)

    @Query("UPDATE  call_log  SET nameFromServer =:nameFromServer, spamCount =:spamCount, color=:typeSpam  WHERE numberFormated =:contactAddress")
    abstract fun updateSpammerWitServerInfo(contactAddress: String, nameFromServer: String, spamCount: Long, typeSpam: Int)

    @Query("UPDATE  call_log  SET name =:name, thumbnailFromCp=:thumbnailFromCp WHERE numberFormated =:contactAddress")
    suspend fun updateWitCproviderInfo(contactAddress: String, name:String, thumbnailFromCp: String)

    @Query("UPDATE  call_log  SET isDeleted=:isDeleted WHERE numberFormated =:num")
    suspend fun markAsDeleted(num:String, isDeleted:Boolean = true)



    @Query("SELECT * FROM call_log WHERE numberFormated LIKE :contactAddress OR name LIKE :contactAddress OR nameFromServer LIKE :contactAddress ORDER BY dateInMilliseconds DESC")
    suspend fun searchCalllog(contactAddress: String): MutableList<CallLogTable>

    @Query("UPDATE  call_log  SET isReportedByUser=:isReportedByUser, spamCount =:spamCount, color =:color WHERE numberFormated =:contactAddress")
    suspend fun markAsReportedByUser(contactAddress: String, spamCount: Long, isReportedByUser:Boolean = true, color:Int = TYPE_SPAM)

    @Query("SELECT * FROM call_log WHERE isDeleted=:isDeleted AND isReportedByUser =:isReportedByUser OR spamCount > :spamLimit ORDER BY dateInMilliseconds DESC ")
    fun getSpamCallLogLivedata(isDeleted: Boolean = false, isReportedByUser: Boolean = true, spamLimit: Long = SPAM_THREASHOLD):LiveData<MutableList<CallLogTable>>


}