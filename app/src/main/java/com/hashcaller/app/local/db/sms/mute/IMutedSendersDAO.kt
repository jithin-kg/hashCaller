package com.hashcaller.app.local.db.sms.mute

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IMutedSendersDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(senders:List<MutedSenders>)

    @Query("SELECT * FROM muted_senders WHERE address LIKE '%'|| :address || '%'")
    suspend fun find(address:String):MutedSenders?
}