package com.nibble.hashcaller.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Created by Jithin KG on 03,July,2020
 */
@Dao
interface BlockedLIstDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockPattern: BlockedListPattern)

    @Query("SELECT * FROM block_list_pattern")
     fun getAllBLockListPattern():LiveData<List<BlockedListPattern>>

    @Query("SELECT * FROM block_list_pattern")
    suspend fun getAllBLockListPatternList():List<BlockedListPattern>

}