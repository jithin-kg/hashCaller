package com.hashcaller.app.local.db.sms.block

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IBlockedOrSpamSendersDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(senders:List<BlockedOrSpamSenders>)

    @Query("SELECT * FROM blocked_or_spam_senders WHERE address LIKE '%'|| :address || '%'")
    suspend fun find(address:String):BlockedOrSpamSenders?
}