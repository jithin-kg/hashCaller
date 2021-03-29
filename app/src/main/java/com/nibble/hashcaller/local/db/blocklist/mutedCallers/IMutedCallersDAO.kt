package com.nibble.hashcaller.local.db.blocklist.mutedCallers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IMutedCallersDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(senders:List<MutedCallers>)

    @Query("SELECT * FROM muted_callers WHERE address= :address")
     fun find(address:String):MutedCallers?
}