package com.hashcaller.app.local.db.update

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import kotlinx.coroutines.flow.Flow

/**
 * Created by Jithin KG on 03,July,2020
 */
@Dao
interface IUpdateAndPriorityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(update: UpdateAndPriority)

    @Query("SELECT * FROM update_and_priority where version_code=:versionCode LIMIT 1")
    suspend fun findByVersion(versionCode:Int):UpdateAndPriority?

    @Query("DELETE FROM update_and_priority ")
    suspend fun deleteAll()
}