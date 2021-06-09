package com.nibble.hashcaller.local.db.blocklist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Created by Jithin KG on 03,July,2020
 */
@Dao
interface BlockedLIstDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockPattern: BlockedListPattern)


    @Query("DELETE from block_list_pattern WHERE num_pattern=:blockPattern AND type=:type")
    suspend fun delete(blockPattern: String, type:Int)
    
    @Query("SELECT * FROM block_list_pattern")
     fun getAllBLockListPattern():LiveData<List<BlockedListPattern>>

    @Query("SELECT * FROM block_list_pattern")
    fun getAllBLockListPatternByFlow():Flow<List<BlockedListPattern>>

    @Query("SELECT * FROM block_list_pattern")
    suspend fun getAllBLockListPatternList():List<BlockedListPattern>

    @Query("SELECT * FROM block_list_pattern WHERE num_pattern =:numberPattern AND type =:type ")
    suspend fun find(numberPattern: String, type: Int):BlockedListPattern

    @Query("DELETE FROM  block_list_pattern")
    suspend fun deleteAll()


}