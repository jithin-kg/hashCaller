package com.nibble.hashcaller.local.db.blocklist.mutedCallers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IMutedCallersDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(senders:List<MutedCallers>)

    @Query("SELECT * FROM muted_callers WHERE address= :address")
     suspend fun find(address:String):MutedCallers?

    @Query("DELETE FROM muted_callers where address=:contactAdders")
     suspend fun delete(contactAdders: String)
}