package com.hashcaller.app.local.db.sms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface SmsOutboxListDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: SMSOutBox)

    @Query("DELETE from sms_outbox WHERE id=:id")
    suspend fun delete(id: String?)

    @Query("DELETE from sms_outbox")
    suspend fun deleteAll()
}