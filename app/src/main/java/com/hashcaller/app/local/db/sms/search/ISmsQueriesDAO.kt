package com.hashcaller.app.local.db.sms.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ISmsQueriesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: SmsSearchQueries)

    @Query("DELETE from sms_search_queries WHERE `query`=:query")
    suspend fun delete(query: String)
    @Query("SELECT * FROM sms_search_queries")
    suspend fun getAll(): List<SmsSearchQueries>
}